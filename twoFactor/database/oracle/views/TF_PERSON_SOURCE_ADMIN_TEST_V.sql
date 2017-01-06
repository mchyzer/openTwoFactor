/* Formatted on 1/6/2017 2:10:44 PM (QP5 v5.252.13127.32847) */
CREATE OR REPLACE FORCE VIEW TF_PERSON_SOURCE_ADMIN_TEST_V
(
   PENN_ID,
   PENNNAME,
   DESCRIPTION,
   SEARCH_DESCRIPTION,
   NAME,
   EMAIL,
   ACTIVE,
   BIRTH_DATE
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
          BIRTH_DATE
     FROM tf_person_source_helper_v
   UNION
   SELECT PENN_ID,
          PENNNAME,
          DESCRIPTION,
          SEARCH_DESCRIPTION,
          NAME,
          EMAIL,
          ACTIVE,
          BIRTH_DATE
     FROM TF_TEST_PERSON;
