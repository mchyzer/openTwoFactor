/* Formatted on 8/11/2017 2:27:30 PM (QP5 v5.252.13127.32847) */
CREATE OR REPLACE FORCE VIEW TF_PERSON_SOURCE_ADMIN_V
(
   PENN_ID,
   PENNNAME,
   DESCRIPTION,
   SEARCH_DESCRIPTION,
   NAME,
   EMAIL,
   ACTIVE,
   BIRTH_DATE,
   LAST_FOUR
)
   BEQUEATH DEFINER
AS
   SELECT PENN_ID,
          PENNNAME,
          DESCRIPTION,
          SEARCH_DESCRIPTION,
          NAME,
          /* (case when pennname = 'mchyzer' then null  
          when pennname = 'dhum' then 'mchyzer@isc.upenn.edu'
          else email end) as email */
          EMAIL,
          'T' AS active,
          BIRTH_DATE,
          last_four
     FROM tf_person_source_helper_v;

COMMENT ON TABLE TF_PERSON_SOURCE_ADMIN_V IS 'person source to lookup and search for subjects in the two factor project for admin users';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.PENN_ID IS 'numeric penn_id';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.PENNNAME IS 'pennkey of the user';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.DESCRIPTION IS 'description to show on screen';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.SEARCH_DESCRIPTION IS 'lower search description to search with';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.NAME IS 'name of user';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.EMAIL IS 'email of user';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.ACTIVE IS 'T if active, F if not';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.BIRTH_DATE IS 'birth date of the user used in optin if it exists for the user';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.LAST_FOUR IS 'last four of SSN if we have it';



GRANT SELECT ON TF_PERSON_SOURCE_ADMIN_V TO TWO_FACTOR_ADMIN;

