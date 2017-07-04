/* Formatted on 7/3/2017 10:57:39 PM (QP5 v5.252.13127.32847) */
CREATE OR REPLACE FORCE VIEW TF_PERSON_SOURCE_TEST_V
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
          DESCRIPTION_PENN AS DESCRIPTION,
          SEARCH_DESCRIPTION_PENN AS SEARCH_DESCRIPTION,
          NAME_PENN AS NAME,
          EMAIL,
          ACTIVE,
          BIRTH_DATE,
          last_four
     FROM TF_PERSON_SOURCE_HELPER_V
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
