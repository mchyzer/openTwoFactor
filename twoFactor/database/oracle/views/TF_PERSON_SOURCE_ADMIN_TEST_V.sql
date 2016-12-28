CREATE OR REPLACE FORCE VIEW TF_PERSON_SOURCE_ADMIN_TEST_V
(
   PENN_ID,
   PENNNAME,
   DESCRIPTION,
   SEARCH_DESCRIPTION,
   NAME,
   EMAIL,
   ACTIVE
)
   BEQUEATH DEFINER
AS
   SELECT PENN_ID,
          PENNNAME,
          DESCRIPTION,
          SEARCH_DESCRIPTION,
          NAME,
          EMAIL,
          ACTIVE
     FROM tf_person_source_helper_v
   union
   SELECT PENN_ID,
          PENNNAME,
          DESCRIPTION,
          SEARCH_DESCRIPTION,
          NAME,
          EMAIL,
          ACTIVE
     FROM TF_TEST_PERSON     
     ;