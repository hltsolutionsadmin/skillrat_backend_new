create table if not exists asset_category (
  id varchar(36) primary key,
  created_date timestamp null,
  updated_date timestamp null,
  created_by varchar(255) null,
  updated_by varchar(255) null,
  tenant_id varchar(64) null,
  code varchar(64) not null unique,
  name varchar(128) not null,
  description varchar(1024)
);

create table if not exists asset (
  id varchar(36) primary key,
  created_date timestamp null,
  updated_date timestamp null,
  created_by varchar(255) null,
  updated_by varchar(255) null,
  tenant_id varchar(64) null,
  category_id varchar(36) not null,
  name varchar(256) not null,
  description varchar(1024),
  storage_key varchar(512) not null,
  mime_type varchar(128),
  size_bytes bigint,
  checksum varchar(128),
  visibility varchar(16) not null,
  owner_type varchar(32) not null,
  owner_id varchar(64) not null,
  constraint fk_asset_category foreign key (category_id) references asset_category(id)
);

create table if not exists asset_inventory (
  id varchar(36) primary key,
  created_date timestamp null,
  updated_date timestamp null,
  created_by varchar(255) null,
  updated_by varchar(255) null,
  tenant_id varchar(64) null,
  asset_id varchar(36) not null,
  location varchar(128),
  quantity_total int not null,
  quantity_available int not null,
  constraint fk_inventory_asset foreign key (asset_id) references asset(id)
);

create table if not exists asset_request (
  id varchar(36) primary key,
  created_date timestamp null,
  updated_date timestamp null,
  created_by varchar(255) null,
  updated_by varchar(255) null,
  tenant_id varchar(64) null,
  asset_id varchar(36) not null,
  requested_by varchar(64) not null,
  quantity int not null,
  status varchar(32) not null,
  constraint fk_request_asset foreign key (asset_id) references asset(id)
);
