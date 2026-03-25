package com.makers.memoir.feature;

import com.makers.memoir.model.Group;
import com.makers.memoir.model.GroupMember;
import com.makers.memoir.model.User;
import com.makers.memoir.repository.GroupMemberRepository;
import com.makers.memoir.repository.GroupRepository;
import com.makers.memoir.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
class GroupFeatureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    private static final String OWNER_EMAIL  = "owner@example.com";
    private static final String MEMBER_EMAIL = "member@example.com";

    private User owner;
    private User member;

    @BeforeEach
    void setUp() {
        owner = userRepository.findUserByEmail(OWNER_EMAIL).orElseGet(() -> {
            User u = new User();
            u.setEmail(OWNER_EMAIL);
            u.setUsername("owneruser");
            u.setFirstname("Owner");
            u.setLastname("User");
            return userRepository.save(u);
        });

        member = userRepository.findUserByEmail(MEMBER_EMAIL).orElseGet(() -> {
            User u = new User();
            u.setEmail(MEMBER_EMAIL);
            u.setUsername("memberuser");
            u.setFirstname("Member");
            u.setLastname("User");
            return userRepository.save(u);
        });
    }

    @AfterEach
    void cleanUp() {
        groupMemberRepository.findByUserId(owner.getId())
                .forEach(gm -> groupMemberRepository.delete(gm));

        groupMemberRepository.findByUserId(member.getId())
                .forEach(gm -> groupMemberRepository.delete(gm));

        groupRepository.findAll().stream()
                .filter(g -> g.getCreatedBy() != null
                        && g.getCreatedBy().getId().equals(owner.getId()))
                .forEach(g -> groupRepository.delete(g));

        userRepository.findUserByEmail(OWNER_EMAIL).ifPresent(userRepository::delete);
        userRepository.findUserByEmail(MEMBER_EMAIL).ifPresent(userRepository::delete);
    }

    @Test
    void authenticatedUserCanViewGroupsPage() throws Exception {
        mockMvc.perform(get("/groups")
                        .with(oidcLogin().idToken(t -> t.claim("email", OWNER_EMAIL))))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/index"));
    }

    @Test
    void unauthenticatedUserIsRedirectedFromGroups() throws Exception {
        mockMvc.perform(get("/groups"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void ownerCanCreateWeeklyGroup() throws Exception {
        mockMvc.perform(post("/groups")
                        .with(oidcLogin().idToken(t -> t.claim("email", OWNER_EMAIL)))
                        .with(csrf())
                        .param("name", "Test Weekly Group")
                        .param("description", "A test group")
                        .param("type", "weekly")
                        .param("sendDate", "2026-12-01T09:00:00"))
                .andExpect(status().is3xxRedirection());

        assertTrue(groupRepository.findAll().stream()
                .anyMatch(g -> g.getName().equals("Test Weekly Group")));
    }

    @Test
    void creatingGroupMakesUserAnOwner() throws Exception {
        mockMvc.perform(post("/groups")
                        .with(oidcLogin().idToken(t -> t.claim("email", OWNER_EMAIL)))
                        .with(csrf())
                        .param("name", "Ownership Test Group")
                        .param("type", "weekly")
                        .param("sendDate", "2026-12-01T09:00:00"))
                .andExpect(status().is3xxRedirection());

        Group created = groupRepository.findAll().stream()
                .filter(g -> g.getName().equals("Ownership Test Group"))
                .findFirst()
                .orElseThrow();

        GroupMember membership = groupMemberRepository
                .findByGroupIdAndUserId(created.getId(), owner.getId())
                .orElseThrow();

        assertEquals("owner", membership.getRole());
        assertEquals("joined", membership.getStatus());
    }

    @Test
    void memberCanViewGroupPage() throws Exception {
        Group group = new Group();
        group.setName("Viewable Group");
        group.setType("weekly");
        group.setCreatedBy(owner);
        groupRepository.save(group);

        GroupMember membership = new GroupMember(group, owner, "owner");
        membership.setStatus("joined");
        groupMemberRepository.save(membership);

        mockMvc.perform(get("/groups/" + group.getId())
                        .with(oidcLogin().idToken(t -> t.claim("email", OWNER_EMAIL))))
                .andExpect(status().isOk())
                .andExpect(view().name("groups/show"));
    }

    @Test
    void ownerCanInviteMember() throws Exception {
        Group group = new Group();
        group.setName("Invite Test Group");
        group.setType("weekly");
        group.setCreatedBy(owner);
        groupRepository.save(group);

        GroupMember ownerMembership = new GroupMember(group, owner, "owner");
        ownerMembership.setStatus("joined");
        groupMemberRepository.save(ownerMembership);

        mockMvc.perform(get("/groups/" + group.getId() + "/invite")
                        .with(oidcLogin().idToken(t -> t.claim("email", OWNER_EMAIL)))
                        .param("userId", member.getId().toString()))
                .andExpect(status().is3xxRedirection());

        assertTrue(groupMemberRepository
                .findByGroupIdAndUserId(group.getId(), member.getId())
                .isPresent());
    }

    @Test
    void invitedMemberHasPendingStatus() throws Exception {
        Group group = new Group();
        group.setName("Pending Status Group");
        group.setType("weekly");
        group.setCreatedBy(owner);
        groupRepository.save(group);

        GroupMember ownerMembership = new GroupMember(group, owner, "owner");
        ownerMembership.setStatus("joined");
        groupMemberRepository.save(ownerMembership);

        mockMvc.perform(get("/groups/" + group.getId() + "/invite")
                        .with(oidcLogin().idToken(t -> t.claim("email", OWNER_EMAIL)))
                        .param("userId", member.getId().toString()))
                .andExpect(status().is3xxRedirection());

        GroupMember invite = groupMemberRepository
                .findByGroupIdAndUserId(group.getId(), member.getId())
                .orElseThrow();

        assertEquals("pending", invite.getStatus());
    }

    @Test
    void memberCanAcceptInvite() throws Exception {
        Group group = new Group();
        group.setName("Accept Invite Group");
        group.setType("weekly");
        group.setCreatedBy(owner);
        groupRepository.save(group);

        GroupMember pending = new GroupMember(group, member, "member");
        pending.setStatus("pending");
        groupMemberRepository.save(pending);

        mockMvc.perform(post("/groups/" + group.getId() + "/accept")
                        .with(oidcLogin().idToken(t -> t.claim("email", MEMBER_EMAIL)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        GroupMember updated = groupMemberRepository
                .findByGroupIdAndUserId(group.getId(), member.getId())
                .orElseThrow();

        assertEquals("joined", updated.getStatus());
    }

    @Test
    void ownerCanDeleteGroup() throws Exception {
        Group group = new Group();
        group.setName("Deletable Group");
        group.setType("weekly");
        group.setCreatedBy(owner);
        groupRepository.save(group);

        GroupMember ownerMembership = new GroupMember(group, owner, "owner");
        ownerMembership.setStatus("joined");
        groupMemberRepository.save(ownerMembership);

        Long groupId = group.getId();

        mockMvc.perform(post("/groups/" + groupId + "/delete")
                        .with(oidcLogin().idToken(t -> t.claim("email", OWNER_EMAIL)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertFalse(groupRepository.findById(groupId).isPresent());
    }

    @Test
    void nonOwnerCannotDeleteGroup() throws Exception {
        Group group = new Group();
        group.setName("Non Deletable Group");
        group.setType("weekly");
        group.setCreatedBy(owner);
        groupRepository.save(group);

        GroupMember ownerMembership = new GroupMember(group, owner, "owner");
        ownerMembership.setStatus("joined");
        groupMemberRepository.save(ownerMembership);

        GroupMember memberMembership = new GroupMember(group, member, "member");
        memberMembership.setStatus("joined");
        groupMemberRepository.save(memberMembership);

        Long groupId = group.getId();

        mockMvc.perform(post("/groups/" + groupId + "/delete")
                        .with(oidcLogin().idToken(t -> t.claim("email", MEMBER_EMAIL)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertTrue(groupRepository.findById(groupId).isPresent());
    }
}