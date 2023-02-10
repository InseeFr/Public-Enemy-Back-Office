insert into questionnaire (context, creation_date, synchronized, label, questionnaire_pogues_id, survey_unit_data, updated_date) values ('HOUSEHOLD', CURRENT_TIMESTAMP(), true, 'questionnaire_label 1', 'l8wwljbo', 'content1', CURRENT_TIMESTAMP());
insert into questionnaire (context, creation_date, synchronized, label, questionnaire_pogues_id, survey_unit_data, updated_date) values ('HOUSEHOLD', CURRENT_TIMESTAMP(), true, 'questionnaire_label 2', 'lqdfgdf', 'content2', CURRENT_TIMESTAMP());
insert into questionnaire (context, creation_date, synchronized, label, questionnaire_pogues_id, survey_unit_data, updated_date) values ('HOUSEHOLD', CURRENT_TIMESTAMP(), true, 'questionnaire_label 3', 'lqdfgdf', 'content3', CURRENT_TIMESTAMP());

insert into questionnaire_mode (questionnaire_id, mode, state) values (1, 'CAWI', 'OK');
insert into questionnaire_mode (questionnaire_id, mode, state) values (1, 'CAPI', 'OK');
insert into questionnaire_mode (questionnaire_id, mode, state) values (2, 'CAWI', 'OK');
insert into questionnaire_mode (questionnaire_id, mode, state) values (3, 'CAWI', 'OK');
