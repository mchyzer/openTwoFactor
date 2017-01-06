/* Formatted on 1/6/2017 2:09:22 PM (QP5 v5.252.13127.32847) */
CREATE OR REPLACE FORCE VIEW TF_PERSON_SOURCE_ALL_V
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
   SELECT penn_id,
          pennname,
          description,
          search_description,
          name,
          email,
          'T' AS active,
          BIRTH_DATE
     FROM tf_person_source_v;

COMMENT ON TABLE TF_PERSON_SOURCE_ALL_V IS 'for test, all people are active';
