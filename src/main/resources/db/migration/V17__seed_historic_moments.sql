-- V__seed_group_6_moments_and_weeklies.sql

-- Create group 6
INSERT INTO groups (id, name, description, type, created_by, created_at)
VALUES (
           6,
           'The Devs',
           'Seeded demo group for historical moments and newsletters',
           'weekly',
           12,
           '2026-03-25 10:34:16.659490'
       );

-- Add group members
INSERT INTO group_members (group_id, user_id, role, status, joined_at)
VALUES
    (6, 12, 'owner',  'joined', '2026-03-25 10:35:00'),
    (6,  9, 'member', 'joined', '2026-03-25 10:35:00'),
    (6, 10, 'member', 'joined', '2026-03-25 10:35:00'),
    (6, 11, 'member', 'joined', '2026-03-25 10:35:00');

-- Seed 14 days of moments for 4 users = 56 moments total
WITH seeded_days AS (
    SELECT generate_series(
                   '2026-03-12 12:00:00'::timestamp,
                   '2026-03-25 12:00:00'::timestamp,
                   interval '1 day'
           ) AS created_at
),
     seed_users AS (
         SELECT *
         FROM (VALUES
                   (9,  'Mark Addis'),
                   (10, 'Jess Watson'),
                   (11, 'Samin Taseen'),
                   (12, 'Reece Ogidih')
              ) AS u(user_id, full_name)
     ),
     inserted_moments AS (
INSERT INTO moments (created_by, image_url, content, location, latitude, longitude, created_at)
SELECT
    u.user_id,
    CASE u.user_id
        WHEN 9 THEN
            CASE EXTRACT(DAY FROM d.created_at)::int
                    WHEN 12 THEN 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800'
                    WHEN 13 THEN 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800'
                    WHEN 14 THEN 'https://images.unsplash.com/photo-1552566626-52f8b828add9?w=800'
                    WHEN 15 THEN 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800'
                    WHEN 16 THEN 'https://images.unsplash.com/photo-1517649763962-0c623066013b?w=800'
                    WHEN 17 THEN 'https://images.unsplash.com/photo-1519996529931-28324d5a630e?w=800'
                    WHEN 18 THEN 'https://images.unsplash.com/photo-1514565131-fce0801e5785?w=800'
                    WHEN 19 THEN 'https://images.unsplash.com/photo-1470770841072-f978cf4d019e?w=800'
                    WHEN 20 THEN 'https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800'
                    WHEN 21 THEN 'https://images.unsplash.com/photo-1559339352-11d035aa65de?w=800'
                    WHEN 22 THEN 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800'
                    WHEN 23 THEN 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=800'
                    WHEN 24 THEN 'https://images.unsplash.com/photo-1522069213448-443a614da9b6?w=800'
                    WHEN 25 THEN 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=800'
END
WHEN 10 THEN
                CASE EXTRACT(DAY FROM d.created_at)::int
                    WHEN 12 THEN 'https://images.unsplash.com/photo-1511920170033-f8396924c348?w=800'
                    WHEN 13 THEN 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=800'
                    WHEN 14 THEN 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800'
                    WHEN 15 THEN 'https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=800'
                    WHEN 16 THEN 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=800'
                    WHEN 17 THEN 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800'
                    WHEN 18 THEN 'https://images.unsplash.com/photo-1489515217757-5fd1be406fef?w=800'
                    WHEN 19 THEN 'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?w=800'
                    WHEN 20 THEN 'https://images.unsplash.com/photo-1500534623283-312aade485b7?w=800'
                    WHEN 21 THEN 'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=800'
                    WHEN 22 THEN 'https://images.unsplash.com/photo-1515377905703-c4788e51af15?w=800'
                    WHEN 23 THEN 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=800'
                    WHEN 24 THEN 'https://images.unsplash.com/photo-1522069213448-443a614da9b6?w=800'
                    WHEN 25 THEN 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800'
END
WHEN 11 THEN
                CASE EXTRACT(DAY FROM d.created_at)::int
                    WHEN 12 THEN 'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?w=800'
                    WHEN 13 THEN 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=800'
                    WHEN 14 THEN 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800'
                    WHEN 15 THEN 'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=800'
                    WHEN 16 THEN 'https://images.unsplash.com/photo-1482192505345-5655af888cc4?w=800'
                    WHEN 17 THEN 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800'
                    WHEN 18 THEN 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800'
                    WHEN 19 THEN 'https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=800'
                    WHEN 20 THEN 'https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800'
                    WHEN 21 THEN 'https://images.unsplash.com/photo-1552566626-52f8b828add9?w=800'
                    WHEN 22 THEN 'https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=800'
                    WHEN 23 THEN 'https://images.unsplash.com/photo-1504384308090-c894fdcc538d?w=800'
                    WHEN 24 THEN 'https://images.unsplash.com/photo-1522069213448-443a614da9b6?w=800'
                    WHEN 25 THEN 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=800'
END
WHEN 12 THEN
                CASE EXTRACT(DAY FROM d.created_at)::int
                    WHEN 12 THEN 'https://images.unsplash.com/photo-1517048676732-d65bc937f952?w=800'
                    WHEN 13 THEN 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800'
                    WHEN 14 THEN 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800'
                    WHEN 15 THEN 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800'
                    WHEN 16 THEN 'https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=800'
                    WHEN 17 THEN 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800'
                    WHEN 18 THEN 'https://images.unsplash.com/photo-1517649763962-0c623066013b?w=800'
                    WHEN 19 THEN 'https://images.unsplash.com/photo-1489515217757-5fd1be406fef?w=800'
                    WHEN 20 THEN 'https://images.unsplash.com/photo-1545239351-1141bd82e8a6?w=800'
                    WHEN 21 THEN 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800'
                    WHEN 22 THEN 'https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=800'
                    WHEN 23 THEN 'https://images.unsplash.com/photo-1504384308090-c894fdcc538d?w=800'
                    WHEN 24 THEN 'https://images.unsplash.com/photo-1522069213448-443a614da9b6?w=800'
                    WHEN 25 THEN 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800'
END
END AS image_url,
        CASE u.user_id
            WHEN 9 THEN
                CASE EXTRACT(DAY FROM d.created_at)::int
                    WHEN 12 THEN 'Stopped for a really good coffee this morning before getting on with the day.'
                    WHEN 13 THEN 'Went for a long walk after work and the weather was better than expected.'
                    WHEN 14 THEN 'Tried a new burger spot tonight and it was definitely worth it.'
                    WHEN 15 THEN 'Beach trip today just to clear my head a bit. Needed that.'
                    WHEN 16 THEN 'Played football and came back absolutely shattered but in a good way.'
                    WHEN 17 THEN 'Family dinner tonight, loads of food and a proper catch-up.'
                    WHEN 18 THEN 'Caught some nice city shots on the way home, the light looked unreal.'
                    WHEN 19 THEN 'Took a quiet walk through the park and actually switched off for once.'
                    WHEN 20 THEN 'Gaming night ended up being way more competitive than expected.'
                    WHEN 21 THEN 'Went out for dinner and stayed talking for hours afterwards.'
                    WHEN 22 THEN 'Slow nature day today, just wanted a break from screens.'
                    WHEN 23 THEN 'Day trip out and about, nice just seeing somewhere different for a change.'
                    WHEN 24 THEN 'Just won minigolf haha! Was a close game, I thought Reece had me at the end!'
                    WHEN 25 THEN 'Rounded the day off with a really solid meal and a relaxed evening.'
END
WHEN 10 THEN
                CASE EXTRACT(DAY FROM d.created_at)::int
                    WHEN 12 THEN 'Started the day with coffee and a quiet moment to plan the week properly.'
                    WHEN 13 THEN 'Took a few photos today and actually loved how they turned out.'
                    WHEN 14 THEN 'Went out for brunch and ended up staying out most of the afternoon.'
                    WHEN 15 THEN 'Spent time reorganising things at home and it made everything feel calmer.'
                    WHEN 16 THEN 'Gym session today and I actually felt really good afterwards.'
                    WHEN 17 THEN 'Baked a cake and it somehow came out better than expected.'
                    WHEN 18 THEN 'Walked through town and stopped every few minutes to take pictures.'
                    WHEN 19 THEN 'Had one of those really peaceful outdoorsy days that resets your head a bit.'
                    WHEN 20 THEN 'Took some time for myself and just slowed everything down for once.'
                    WHEN 21 THEN 'Met up with everyone and had such a fun evening out.'
                    WHEN 22 THEN 'Stayed home, rested properly, and let myself have a lazy day.'
                    WHEN 23 THEN 'Live music tonight and the atmosphere was so nice.'
                    WHEN 24 THEN 'Could not stop laughing at mini golf, genuinely one of the best parts of the week.'
                    WHEN 25 THEN 'Ended the day with really good food and a nice sense of balance.'
END
WHEN 11 THEN
                CASE EXTRACT(DAY FROM d.created_at)::int
                    WHEN 12 THEN 'Took a walk early and the whole day felt clearer afterwards.'
                    WHEN 13 THEN 'Spent some time reading and properly slowing down for a bit.'
                    WHEN 14 THEN 'Quiet nature day today, just wanted a bit of peace and fresh air.'
                    WHEN 15 THEN 'Walked through the woods and ended up staying out way longer than planned.'
                    WHEN 16 THEN 'Cafe stop and some time sorting life admin. Weirdly satisfying.'
                    WHEN 17 THEN 'Productive day overall and managed to make proper progress on a few things.'
                    WHEN 18 THEN 'Got outside for a while and appreciated having a bit of open space.'
                    WHEN 19 THEN 'Had a long catch-up with someone and it helped more than expected.'
                    WHEN 20 THEN 'Gaming for a bit tonight and properly switched off.'
                    WHEN 21 THEN 'Went out for food and it turned into one of those unexpectedly good evenings.'
                    WHEN 22 THEN 'Slow home day today. Needed a reset more than I realised.'
                    WHEN 23 THEN 'Spent part of the day planning the next few weeks and getting organised.'
                    WHEN 24 THEN 'Really fun evening, mini golf got more competitive than I expected.'
                    WHEN 25 THEN 'Finished the day with a good meal and a calm evening at home.'
END
WHEN 12 THEN
                CASE EXTRACT(DAY FROM d.created_at)::int
                    WHEN 12 THEN 'Spent some time talking through plans and ideas, which actually helped make things feel more concrete.'
                    WHEN 13 THEN 'Went out for a walk and ended up staying out longer than planned.'
                    WHEN 14 THEN 'Beach day today, mostly just to get out properly and reset a bit.'
                    WHEN 15 THEN 'Went to an event in the evening and the atmosphere was really good.'
                    WHEN 16 THEN 'Spent a lot of today thinking through direction and what is actually worth building.'
                    WHEN 17 THEN 'Caught up with people and had a surprisingly productive conversation out of it.'
                    WHEN 18 THEN 'Played sport today and it was good to do something physical for a change.'
                    WHEN 19 THEN 'Walked around town for a bit and gave myself time to think.'
                    WHEN 20 THEN 'Tried a climbing session and definitely felt it afterwards.'
                    WHEN 21 THEN 'Cinema tonight. Nice just sitting back and switching off for a while.'
                    WHEN 22 THEN 'Slower day today, mostly resting and taking things easy at home.'
                    WHEN 23 THEN 'Spent some time planning ahead and thinking about the next steps properly.'
                    WHEN 24 THEN 'Mini golf got weirdly competitive but was actually a really good laugh.'
                    WHEN 25 THEN 'Ended the day with a nice dinner and the feeling that the last couple of weeks had real momentum.'
END
END AS content,
        CASE u.user_id
            WHEN 9 THEN
                CASE EXTRACT(DAY FROM d.created_at)::int
                    WHEN 12 THEN 'Shoreditch'
                    WHEN 13 THEN 'Canary Wharf'
                    WHEN 14 THEN 'Camden'
                    WHEN 15 THEN 'Brighton Beach'
                    WHEN 16 THEN 'London Fields'
                    WHEN 17 THEN 'Family Home'
                    WHEN 18 THEN 'South Bank'
                    WHEN 19 THEN 'Hyde Park'
                    WHEN 20 THEN 'Home'
                    WHEN 21 THEN 'Brixton'
                    WHEN 22 THEN 'Richmond Park'
                    WHEN 23 THEN 'Oxford'
                    WHEN 24 THEN 'The Lost Jungle London'
                    WHEN 25 THEN 'Soho'
END
WHEN 10 THEN
                CASE EXTRACT(DAY FROM d.created_at)::int
                    WHEN 12 THEN 'Home'
                    WHEN 13 THEN 'Northern Quarter'
                    WHEN 14 THEN 'Manchester'
                    WHEN 15 THEN 'Home'
                    WHEN 16 THEN 'Manchester Gym'
                    WHEN 17 THEN 'Home'
                    WHEN 18 THEN 'Deansgate'
                    WHEN 19 THEN 'Peak District'
                    WHEN 20 THEN 'Home'
                    WHEN 21 THEN 'Ancoats'
                    WHEN 22 THEN 'Home'
                    WHEN 23 THEN 'Manchester Academy'
                    WHEN 24 THEN 'The Lost Jungle London'
                    WHEN 25 THEN 'Home'
END
WHEN 11 THEN
                CASE EXTRACT(DAY FROM d.created_at)::int
                    WHEN 12 THEN 'Clifton'
                    WHEN 13 THEN 'Home'
                    WHEN 14 THEN 'Leigh Woods'
                    WHEN 15 THEN 'Ashton Court'
                    WHEN 16 THEN 'Clifton Village'
                    WHEN 17 THEN 'Bristol'
                    WHEN 18 THEN 'Harbourside'
                    WHEN 19 THEN 'Home'
                    WHEN 20 THEN 'Home'
                    WHEN 21 THEN 'Bristol'
                    WHEN 22 THEN 'Home'
                    WHEN 23 THEN 'Bristol'
                    WHEN 24 THEN 'The Lost Jungle London'
                    WHEN 25 THEN 'Home'
END
WHEN 12 THEN
                CASE EXTRACT(DAY FROM d.created_at)::int
                    WHEN 12 THEN 'Bath'
                    WHEN 13 THEN 'Bath'
                    WHEN 14 THEN 'West Wittering'
                    WHEN 15 THEN 'London'
                    WHEN 16 THEN 'Bath'
                    WHEN 17 THEN 'Bath Spa'
                    WHEN 18 THEN 'Bath Sports Centre'
                    WHEN 19 THEN 'Bath City Centre'
                    WHEN 20 THEN 'Bath Climbing Centre'
                    WHEN 21 THEN 'Cinema'
                    WHEN 22 THEN 'Home'
                    WHEN 23 THEN 'Bath'
                    WHEN 24 THEN 'The Lost Jungle London'
                    WHEN 25 THEN 'Bath'
END
END AS location,
        NULL::double precision,
        NULL::double precision,
        d.created_at
    FROM seed_users u
    CROSS JOIN seeded_days d
    RETURNING id, created_by, created_at
)
INSERT INTO moment_groups (moment_id, group_id)
SELECT id, 6
FROM inserted_moments;

-- Create weekly newsletters spanning all seeded dates
INSERT INTO weekly (group_id, week_start, send_date, status, title, sent_at, pdf_url, html_content)
VALUES
    (
        6,
        '2026-03-09 00:00:00',
        '2026-03-16 09:00:00',
        'sent',
        'The Devs Weekly - 9 Mar 2026',
        '2026-03-16 09:00:00',
        NULL,
        NULL
    ),
    (
        6,
        '2026-03-16 00:00:00',
        '2026-03-23 09:00:00',
        'sent',
        'The Devs Weekly - 16 Mar 2026',
        '2026-03-23 09:00:00',
        NULL,
        NULL
    ),
    (
        6,
        '2026-03-23 00:00:00',
        '2026-03-30 09:00:00',
        'open',
        'The Devs Weekly - 23 Mar 2026',
        NULL,
        NULL,
        NULL
    );

-- Keep the sequence aligned after manually inserting group id 6
SELECT setval('groups_id_seq', (SELECT MAX(id) FROM groups));