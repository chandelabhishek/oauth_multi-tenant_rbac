-- roles
insert into role("id", "name") values ('1bcbe672-fe32-43c4-bd27-b0d321e241ca', 'AGENCY_ADMIN');
insert into role("id", "name") values ('8419b047-1422-426f-bdc1-dca060089837', 'SYSTEM_ADMIN');
insert into role("id", "name") values ('cf6264ad-eda1-43f4-9717-6ff06bc2a3d5', 'TENANT_ADMIN');


-----------------------   creating super tenant, which will manage all the other tenants -------------------------

-- super_user -- pass: yepyep
insert into "user"("id" ,"email", "first_name", "last_name", "phone_number", "country_code", "password", "status") values ('fe7590bd-4347-44ba-adb4-bf8e9876b6ed', 'chandelabhishek008.com', 'Abhishek', 'chandel', '7415333569', '+91', '$2a$10$84NQWDqWmHfgYgLxRuvGGuRuxUeTomxjkpKUpXPZUokLvJG6JbQXq', 'ACTIVE');

-- super tenant
insert into tenant("id", "email", "name", "phone_number", "country_code", "type") values ('968c7527-d8ed-411c-ac52-7e6867fa7831', 'chandelabhishek008.com', 'Abhishek LLC', '7415333459', '+91', 'SUPER_TENANT');

-- super tenant's role
insert into user_tenant("id", "role_id", "tenant_id", "user_id", status) values ('900022e4-3d2e-4183-ba5d-533bf8fec231', '8419b047-1422-426f-bdc1-dca060089837', '968c7527-d8ed-411c-ac52-7e6867fa7831', 'fe7590bd-4347-44ba-adb4-bf8e9876b6ed', 'ACTIVE');

-------------------------------------------------------------------------------------------------------------------