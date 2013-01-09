/*==============================================================*/
/* DBMS name:      PostgreSQL 8                                 */
/* Created on:     11/6/2012 5:21:26 PM                         */
/*==============================================================*/

/*==============================================================*/
/* Table: ATTACH                                                */
/*==============================================================*/
create table ATTACH (
   ATTACHID             SERIAL               not null,
   MESSAGEID            INT4                 not null,
   FILE_PATH            VARCHAR(1024)        not null,
   constraint PK_ATTACH primary key (ATTACHID)
);

/*==============================================================*/
/* Index: ATTACH_PK                                             */
/*==============================================================*/
create unique index ATTACH_PK on ATTACH (
ATTACHID
);

/*==============================================================*/
/* Table: COMMENT                                               */
/*==============================================================*/
create table COMMENT (
   COMMENTID            SERIAL               not null,
   POSTID               INT4                 not null,
   USERID               INT4                 not null,
   SENDING              TIMESTAMP            not null,
   CONTENT              VARCHAR(1024)        not null,
   constraint PK_COMMENT primary key (COMMENTID)
);

/*==============================================================*/
/* Index: POSTCOMMENT_PK                                        */
/*==============================================================*/
create index POSTCOMMENT_PK on COMMENT (
POSTID
);

/*==============================================================*/
/* Index: CONTAINS_FK                                           */
/*==============================================================*/
create  index CONTAINS_FK on ATTACH (
MESSAGEID
);

/*==============================================================*/
/* Table: CHATROOM                                              */
/*==============================================================*/
create table CHATROOM (
   CHATID               SERIAL               not null,
   OWNERID              INT4                 not null,
   NAME                 VARCHAR(1024)        not null,
   RATING               INT4                 null,
   SUBJECT              VARCHAR(1024)        not null,
   ISCLOSED             BOOL                 not null,
   constraint PK_CHATROOM primary key (CHATID),
   constraint subject_type CHECK (SUBJECT IN ('economia','mundo','cultura','desporto','ciencia','tecnologia','multimedia','musica'))
);

/*==============================================================*/
/* Index: CHATROOM_PK                                           */
/*==============================================================*/
create unique index CHATROOM_PK on CHATROOM (
CHATID
);

/*==============================================================*/
/* Index: OWNS_FK                                               */
/*==============================================================*/
create  index OWNS_FK on CHATROOM (
OWNERID
);

/*==============================================================*/
/* Table: CONNECTION                                            */
/*==============================================================*/
create table CONNECTION (
   CHATID               INT4                 not null,
   USERID               INT4                 not null,
   TYPE                 VARCHAR(1024)        not null,
   constraint PK_CONNECTION primary key (CHATID, USERID),
   constraint message_type CHECK (type IN ('poster','watcher'))
);

/*==============================================================*/
/* Index: CONNECTION_PK                                         */
/*==============================================================*/
create unique index CONNECTION_PK on CONNECTION (
CHATID,
USERID
);

/*==============================================================*/
/* Index: LINKS_FK                                              */
/*==============================================================*/
create  index LINKS_FK on CONNECTION (
CHATID
);

/*==============================================================*/
/* Index: HAS_FK                                                */
/*==============================================================*/
create  index HAS_FK on CONNECTION (
USERID
);

/*==============================================================*/
/* Table: MESSAGE                                               */
/*==============================================================*/
create table MESSAGE (
   MESSAGEID            SERIAL               not null,
   FACEBOOKID           VARCHAR(1024)        null,
   SENDERID             INT4                 not null,
   SENDING              TIMESTAMP            not null,
   CONTENT              VARCHAR(1024)        not null,
   RECEIVERID           INT4                 null,
   RECEIVING            TIMESTAMP            null,
   LAST_ACTIVITY        TIMESTAMP            not null,
   TYPE                 VARCHAR(8)           not null,
   constraint PK_MESSAGE primary key (MESSAGEID),
   constraint message_type CHECK (type IN ('public','private','post'))
);

/*==============================================================*/
/* Index: MESSAGE_PK                                            */
/*==============================================================*/
create unique index MESSAGE_PK on MESSAGE (
MESSAGEID
);

/*==============================================================*/
/* Table: USERS                                                */
/*==============================================================*/
create table USERS (
   USERID               SERIAL               not null,
   NAME                 VARCHAR(1024)        not null,
   EMAIL                VARCHAR(1024)        not null,
   FACEBOOKID           VARCHAR(1024)        null,
   CITY                 VARCHAR(1024)        null,
   COUNTRY              VARCHAR(1024)        null,
   BDAY                 DATE                 null,
   SEX                  VARCHAR(1024)        null,
   RECOVERING           VARCHAR(1024)        null,
   PASSWORD             VARCHAR(1024)        not null,
   ISACTIVE             BOOL                 not null,
   ISPUBLIC             BOOL                 not null,
   constraint PK_USER primary key (USERID),
   constraint ID_EMAIL unique (EMAIL)
);

/*==============================================================*/
/* Index: USER_PK                                               */
/*==============================================================*/
create unique index USER_PK on USERS (
USERID
);

/*==============================================================*/
/* Index: EMAIL                                                 */
/*==============================================================*/
create unique index EMAIL on USERS (
EMAIL
);

/*==============================================================*/
/* Table: VOTE                                                  */
/*==============================================================*/
create table VOTE (
   CHATID               INT4                 not null,
   USERID               INT4                 not null,
   RATE                 INT4                 not null,
   constraint PK_VOTE primary key (CHATID, USERID),
   constraint vote_rate CHECK (rate IN (1, 2, 3))
);

/*==============================================================*/
/* Index: VOTE_PK                                               */
/*==============================================================*/
create unique index VOTE_PK on VOTE (
CHATID,
USERID
);

/*==============================================================*/
/* Index: TO_FK                                                 */
/*==============================================================*/
create  index TO_FK on VOTE (
CHATID
);

/*==============================================================*/
/* Index: GIVES_FK                                              */
/*==============================================================*/
create  index GIVES_FK on VOTE (
USERID
);

alter table ATTACH
   add constraint FK_ATTACH_CONTAINS_MESSAGE foreign key (MESSAGEID)
      references MESSAGE (MESSAGEID)
      on delete restrict on update restrict;

alter table CHATROOM
   add constraint FK_CHATROOM_OWNS_USER foreign key (OWNERID)
      references USERS (USERID)
      on delete restrict on update restrict;

alter table CONNECTION
   add constraint FK_CONNECTI_HAS_USER foreign key (USERID)
      references USERS (USERID)
      on delete restrict on update restrict;

alter table CONNECTION
   add constraint FK_CONNECTI_LINKS_CHATROOM foreign key (CHATID)
      references CHATROOM (CHATID)
      on delete restrict on update restrict;

alter table MESSAGE
   add constraint FK_MESSAGE_SENDS_USER foreign key (SENDERID)
      references USERS (USERID)
      on delete restrict on update restrict;

alter table VOTE
   add constraint FK_VOTE_GIVES_USER foreign key (USERID)
      references USERS (USERID)
      on delete restrict on update restrict;

alter table VOTE
   add constraint FK_VOTE_TO_CHATROOM foreign key (CHATID)
      references CHATROOM (CHATID)
      on delete restrict on update restrict;
