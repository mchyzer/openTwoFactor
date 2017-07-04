/* Formatted on 7/3/2017 11:10:01 PM (QP5 v5.252.13127.32847) */
CREATE OR REPLACE FORCE VIEW TF_PERSON_SOURCE_ADMIN_ALL_V
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
   SELECT penn_id,
          pennname,
          description,
          search_description,
          name,
          email,
          'T' AS active,
          birth_date,
          last_four
     FROM tf_person_source_admin_v;

COMMENT ON TABLE TF_PERSON_SOURCE_ADMIN_ALL_V IS 'all view for testing where everyone is active';

GRANT SELECT ON TF_PERSON_SOURCE_ADMIN_ALL_V TO TWO_FACTOR_ADMIN WITH GRANT OPTION;
