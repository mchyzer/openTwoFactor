/* Formatted on 1/15/2019 9:22:45 AM (QP5 v5.252.13127.32847) */
CREATE OR REPLACE FORCE VIEW TWO_STEP_COUNTDOWN_LOADER_V
(
   SUBJECT_ID,
   GROUP_NAME,
   DATE_REQUIRED,
   ALREADY_ENROLLED
)
   BEQUEATH DEFINER
AS
   SELECT subject_id,
          (   'penn:isc:ait:apps:twoFactor:groups:twoFactorCountdown:twoFactorCountdown_'
           || CASE
                 WHEN tcmv.DAYS_TIL_REQUIRED <= 0 THEN '0'
                 ELSE TO_CHAR (tcmv.DAYS_TIL_REQUIRED)
              END)
             AS group_name,
          date_required,
          ALREADY_ENROLLED
     FROM TWO_STEP_COUNTDOWN_MEM2_V tcmv
    WHERE     tcmv.DAYS_TIL_REQUIRED <= 9
          AND (   (TCMV.ALREADY_ENROLLED = 'F' AND TCMV.DAYS_TIL_REQUIRED > 0)
               OR TCMV.DAYS_TIL_REQUIRED <= 0);
