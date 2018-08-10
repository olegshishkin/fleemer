create table if not exists `person`
(
  `id`            bigint unsigned auto_increment unique primary key,
  `first_name`    varchar(255)    not null,
  `last_name`     varchar(255)    null,
  `email`         varchar(255)    not null unique,
  `hash`          varchar(255)    not null
) engine = InnoDB;

create table if not exists `account`
(
  `id`            bigint unsigned auto_increment unique primary key,
  `type`          tinyint         not null,
  `currency`      tinyint         not null,
  `name`          varchar(255)    not null,
  `balance`       decimal(20, 10) not null,
  `person_id`     bigint unsigned not null,
  foreign key (`person_id`) references `person` (`id`),
  index (`person_id`)
) engine = InnoDB;

create table if not exists `category`
(
  `id`            bigint unsigned auto_increment unique primary key,
  `name`          varchar(255)    not null,
  `type`          tinyint         not null,
  `person_id`     bigint unsigned not null,
  foreign key (`person_id`) references `person` (`id`),
  index (`person_id`)
) engine = InnoDB;

create table if not exists `operation`
(
  `id`              bigint unsigned auto_increment unique primary key,
  `date`            date            not null,
  `in_account_id`   bigint unsigned null,
  `out_account_id`  bigint unsigned null,
  `category_id`     bigint unsigned null,
  `sum`             decimal(20, 10) not null,
  `comment`         varchar(255)    null,
  foreign key (`in_account_id`) references `account` (`id`),
  foreign key (`out_account_id`) references `account` (`id`),
  foreign key (`category_id`) references `category` (`id`),
  index (`in_account_id`),
  index (`out_account_id`),
  index (`category_id`)
) engine = InnoDB;
