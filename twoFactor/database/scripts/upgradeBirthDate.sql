ALTER TABLE TWO_FACTOR_ADMIN_FASTPENNCOMM.TF_TEST_PERSON
ADD (BIRTH_DATE DATE);

COMMENT ON COLUMN 
TWO_FACTOR_ADMIN_FASTPENNCOMM.TF_TEST_PERSON.BIRTH_DATE IS 
'birth date for user if not null then use that on opt in';


ALTER TABLE TWO_FACTOR_ADMIN_FASTPENNCOMM.TF_TEST_PERSON_ADMIN
ADD (BIRTH_DATE DATE);

COMMENT ON COLUMN 
TWO_FACTOR_ADMIN_FASTPENNCOMM.TF_TEST_PERSON_ADMIN.BIRTH_DATE IS 
'birth date for user if not null then use that on opt in';
