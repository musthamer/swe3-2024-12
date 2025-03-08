drop table if exists demo;
create table demo (
  id integer primary key auto_increment,
  name text,
  index(name)
);
