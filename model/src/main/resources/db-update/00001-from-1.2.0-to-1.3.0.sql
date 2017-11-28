-- ## Updates required by new artifact repository relations
create table TargetRepository (
    id integer not null,
    identifier varchar(255) not null,
    repositoryPath varchar(255) not null,
    repositoryType varchar(255) not null,
    primary key (id)
);
-- insert default repositories
insert into TargetRepository (id, identifier, repositoryPath, repositoryType) values (1, 'indy-maven', 'builds-untested', 'MAVEN');
insert into TargetRepository (id, identifier, repositoryPath, repositoryType) values (2, 'indy-maven', 'builds-untested-temp', 'MAVEN_TEMPORAL');
insert into TargetRepository (id, identifier, repositoryPath, repositoryType) values (3, 'indy-maven', 'shared-imports', 'MAVEN');
insert into TargetRepository (id, identifier, repositoryPath, repositoryType) values (4, 'indy-maven', 'shared-imports', 'MAVEN_TEMPORAL');
insert into TargetRepository (id, identifier, repositoryPath, repositoryType) values (5, 'indy-http', '', 'GENERIC_PROXY');

alter table Artifact add targetRepository_id integer;

-- migrate data
-- old repotype 0 -> maven (1)
-- old repotype 3 -> generic proxy (5)
update Artifact set targetRepository_id = 1 where repotype = 0;
update Artifact set targetRepository_id = 5 where repotype = 3;
alter table Artifact alter column targetRepository_id set not null;
alter table Artifact drop column repotype;
