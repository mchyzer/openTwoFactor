/* Formatted on 7/3/2017 11:06:42 PM (QP5 v5.252.13127.32847) */
CREATE OR REPLACE FORCE VIEW TF_PERSON_SOURCE_ADMIN_TEST_V
(
   PENN_ID,
   PENNNAME,
   DESCRIPTION,
   SEARCH_DESCRIPTION,
   NAME,
   EMAIL,
   ACTIVE,
   BIRTH_DATE,
   last_four
)
   BEQUEATH DEFINER
AS
   SELECT PENN_ID,
          PENNNAME,
          DESCRIPTION,
          SEARCH_DESCRIPTION,
          NAME,
          EMAIL,
          ACTIVE,
          BIRTH_DATE,
          last_four
     FROM tf_person_source_helper_v
   UNION
   SELECT PENN_ID,
          PENNNAME,
          DESCRIPTION,
          SEARCH_DESCRIPTION,
          NAME,
          EMAIL,
          ACTIVE,
          TO_CHAR (BIRTH_DATE, 'YYYY-MM-DD'),
          last_four
     FROM TF_TEST_PERSON;
