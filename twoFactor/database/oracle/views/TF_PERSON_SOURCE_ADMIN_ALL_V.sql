/* Formatted on 1/6/2017 2:04:20 PM (QP5 v5.252.13127.32847) */
CREATE OR REPLACE FORCE VIEW TF_PERSON_SOURCE_ADMIN_ALL_V
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
          birth_date
     FROM tf_person_source_admin_v;

COMMENT ON TABLE TF_PERSON_SOURCE_ADMIN_ALL_V IS 'all view for testing where everyone is active';
