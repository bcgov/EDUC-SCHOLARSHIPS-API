TRUNCATE TABLE STUDENT@sldlink.world;

--Copy tables into the migration schema with the following fields
SELECT sdc_student.* FROM SDC_SCHOOL_COLLECTION_STUDENT sdc_student, SDC_SCHOOL_COLLECTION school
WHERE sdc_student.SDC_SCHOOL_COLLECTION_ID = school.SDC_SCHOOL_COLLECTION_ID
AND school.COLLECTION_ID = :collectionID;

SELECT * FROM SDC_SCHOOL_COLLECTION school
WHERE school.COLLECTION_ID = :collectionID;

SELECT * FROM INDEPENDENT_SCHOOL_FUNDING_GROUP_SNAPSHOT funding
WHERE funding.COLLECTION_ID = :collectionID;

--Copy both DISTRICT and SCHOOL tables from the target environment into the migration schema
--Copy both SCHOOL_FUNDING_GROUP_CODE table from SDC API into the migration schema

--Run in migration schema
CREATE TABLE SLD_STUDENT_SCHOLARSHIPS_PUMP
AS
SELECT
    sdc_student.ASSIGNED_PEN as STUDENT_ID,
    sdc_student.ASSIGNED_PEN as STUD_NO,
    (SELECT dist.DISTRICT_NUMBER from DISTRICT dist, SCHOOL school WHERE school.SCHOOL_ID = sdc_school.SCHOOL_ID AND dist.DISTRICT_ID = school.DISTRICT_ID) as DISTNO,
    (SELECT school.SCHOOL_NUMBER from SCHOOL school WHERE school.SCHOOL_ID = sdc_school.SCHOOL_ID) as SCHLNO,
    sdc_student.ENROLLED_GRADE_CODE as ENROLLED_GRADE_CODE,
    sdc_student.SCHOOL_FUNDING_CODE as SCHOOL_FUNDING_CODE,
    sdc_student.DOB as BIRTH_DATE,
    sdc_student.FTE * 100 as FTE,
    :enterReportDate as REPORT_DATE,
    'SCHOLARSHIPS' as CREATE_USER,
    sdc_student.CREATE_DATE as CREATE_DATE,
    'SCHOLARSHIPS' as UPDATE_USER,
    sdc_student.UPDATE_DATE as UPDATE_DATE
FROM SDC_SCHOOL_COLLECTION_STUDENT sdc_student, SDC_SCHOOL_COLLECTION sdc_school
WHERE sdc_student.SDC_SCHOOL_COLLECTION_ID = sdc_school.SDC_SCHOOL_COLLECTION_ID
AND sdc_school.COLLECTION_ID = :collectionID
AND sdc_student.SDC_SCHOOL_COLLECTION_STUDENT_STATUS_CODE = 'COMPLETED';

TRUNCATE TABLE SCHOOL_FUNDING_MASTER@sldlink.world;

--Run in migration schema
CREATE TABLE SCHOOL_FUNDING_MASTER_PUMP
AS
WITH funding_counts AS (
    SELECT
        SCHOOL_ID,
        SCHOOL_FUNDING_GROUP_CODE,
        COUNT(*) AS code_count,
        ROW_NUMBER() OVER (
            PARTITION BY school_ID 
            ORDER BY COUNT(*) DESC, SCHOOL_FUNDING_GROUP_CODE
        ) AS rn
    FROM INDEPENDENT_SCHOOL_FUNDING_GROUP_SNAPSHOT
    WHERE collection_id = :collectionID
    AND SCHOOL_GRADE_CODE in ('GRADE11','GRADE12')
    GROUP BY school_ID, SCHOOL_FUNDING_GROUP_CODE
)
SELECT
    (SELECT dist.DISTRICT_NUMBER from DISTRICT dist, SCHOOL school WHERE school.SCHOOL_ID = fund_counts.SCHOOL_ID AND dist.DISTRICT_ID = school.DISTRICT_ID) as DISTNO,
    (SELECT school.SCHOOL_NUMBER from SCHOOL school WHERE school.SCHOOL_ID = fund_counts.SCHOOL_ID) as SCHLNO,
    (SELECT fund_group.LEGACY_CODE from SCHOOL_FUNDING_GROUP_CODE fund_group WHERE fund_group.SCHOOL_FUNDING_GROUP_CODE = fund_counts.SCHOOL_FUNDING_GROUP_CODE) as FUNDING_GROUP_CODE,
    '11' AS FUNDING_GROUP_SUBCODE,
    '20250101' as CREATE_DATE,
    '11134847' as CREATE_TIME,
    'SCHOLARS' as CREATE_USERNAME,
    '20250101' as EDIT_DATE,
    'SCHOLARS' as EDIT_TIME,
    EDIT_USERNAME
FROM FUNDING_COUNTS fund_counts
WHERE rn = 1
ORDER BY school_ID;

