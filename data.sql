--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.6
-- Dumped by pg_dump version 9.1.6
-- Started on 2012-12-16 03:27:32 WET

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 173 (class 3079 OID 11681)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 1986 (class 0 OID 0)
-- Dependencies: 173
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;


DROP FUNCTION IF EXISTS calcrating() CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS message CASCADE;
DROP TABLE IF EXISTS chatroom CASCADE;
DROP TABLE IF EXISTS vote CASCADE;
DROP TABLE IF EXISTS connection CASCADE;
DROP TABLE IF EXISTS attach CASCADE;
DROP TABLE IF EXISTS comment CASCADE;

--
-- TOC entry 185 (class 1255 OID 50632)
-- Dependencies: 6 527
-- Name: calcrating(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION calcrating() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
declare
	c cursor is
		select rate from vote where chatid = NEW.chatid;
	sum decimal := 0;
	rate chatroom.rating % type;
begin
	open c;
	loop
		fetch c into rate;
		exit when not found;
		sum := sum + rate;
	end loop;
	sum := sum / (select count(*) from vote where chatid = NEW.CHATID);
	update chatroom set rating = round(sum, 0) where chatid = NEW.CHATID;
	return null;
end;
$$;


ALTER FUNCTION public.calcrating() OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 162 (class 1259 OID 42296)
-- Dependencies: 6
-- Name: attach; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE attach (
    attachid integer NOT NULL,
    messageid integer NOT NULL,
    file_path character varying(1024) NOT NULL
);


ALTER TABLE public.attach OWNER TO postgres;

--
-- TOC entry 161 (class 1259 OID 42294)
-- Dependencies: 162 6
-- Name: attach_attachid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE attach_attachid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.attach_attachid_seq OWNER TO postgres;

--
-- TOC entry 1987 (class 0 OID 0)
-- Dependencies: 161
-- Name: attach_attachid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE attach_attachid_seq OWNED BY attach.attachid;


--
-- TOC entry 1988 (class 0 OID 0)
-- Dependencies: 161
-- Name: attach_attachid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('attach_attachid_seq', 26, true);


--
-- TOC entry 166 (class 1259 OID 42321)
-- Dependencies: 1930 6
-- Name: chatroom; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE chatroom (
    chatid integer NOT NULL,
    ownerid integer NOT NULL,
    name character varying(1024) NOT NULL,
    rating integer,
    subject character varying(1024) NOT NULL,
    isclosed boolean NOT NULL,
    CONSTRAINT subject_type CHECK (((subject)::text = ANY ((ARRAY['economia'::character varying, 'mundo'::character varying, 'cultura'::character varying, 'desporto'::character varying, 'ciencia'::character varying, 'tecnologia'::character varying, 'multimedia'::character varying, 'musica'::character varying])::text[])))
);


ALTER TABLE public.chatroom OWNER TO postgres;

--
-- TOC entry 165 (class 1259 OID 42319)
-- Dependencies: 6 166
-- Name: chatroom_chatid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE chatroom_chatid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.chatroom_chatid_seq OWNER TO postgres;

--
-- TOC entry 1989 (class 0 OID 0)
-- Dependencies: 165
-- Name: chatroom_chatid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE chatroom_chatid_seq OWNED BY chatroom.chatid;


--
-- TOC entry 1990 (class 0 OID 0)
-- Dependencies: 165
-- Name: chatroom_chatid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('chatroom_chatid_seq', 7, true);


--
-- TOC entry 164 (class 1259 OID 42308)
-- Dependencies: 6
-- Name: comment; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE comment (
    commentid integer NOT NULL,
    postid integer NOT NULL,
    userid integer NOT NULL,
    sending timestamp without time zone NOT NULL,
    content character varying(1024) NOT NULL
);


ALTER TABLE public.comment OWNER TO postgres;

--
-- TOC entry 163 (class 1259 OID 42306)
-- Dependencies: 164 6
-- Name: comment_commentid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE comment_commentid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.comment_commentid_seq OWNER TO postgres;

--
-- TOC entry 1991 (class 0 OID 0)
-- Dependencies: 163
-- Name: comment_commentid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE comment_commentid_seq OWNED BY comment.commentid;


--
-- TOC entry 1992 (class 0 OID 0)
-- Dependencies: 163
-- Name: comment_commentid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('comment_commentid_seq', 20, true);


--
-- TOC entry 167 (class 1259 OID 42333)
-- Dependencies: 1931 6
-- Name: connection; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE connection (
    chatid integer NOT NULL,
    userid integer NOT NULL,
    type character varying(1024) NOT NULL,
    CONSTRAINT message_type CHECK (((type)::text = ANY ((ARRAY['poster'::character varying, 'watcher'::character varying])::text[])))
);


ALTER TABLE public.connection OWNER TO postgres;

--
-- TOC entry 169 (class 1259 OID 42347)
-- Dependencies: 1933 6
-- Name: message; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE message (
    messageid integer NOT NULL,
    facebookid character varying(1024),
    senderid integer NOT NULL,
    sending timestamp without time zone NOT NULL,
    content character varying(1024) NOT NULL,
    receiverid integer,
    receiving timestamp without time zone,
    last_activity timestamp without time zone NOT NULL,
    type character varying(8) NOT NULL,
    CONSTRAINT message_type CHECK (((type)::text = ANY ((ARRAY['public'::character varying, 'private'::character varying, 'post'::character varying])::text[])))
);


ALTER TABLE public.message OWNER TO postgres;

--
-- TOC entry 168 (class 1259 OID 42345)
-- Dependencies: 169 6
-- Name: message_messageid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE message_messageid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.message_messageid_seq OWNER TO postgres;

--
-- TOC entry 1993 (class 0 OID 0)
-- Dependencies: 168
-- Name: message_messageid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE message_messageid_seq OWNED BY message.messageid;


--
-- TOC entry 1994 (class 0 OID 0)
-- Dependencies: 168
-- Name: message_messageid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('message_messageid_seq', 314, true);


--
-- TOC entry 171 (class 1259 OID 42360)
-- Dependencies: 6
-- Name: users; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE users (
    userid integer NOT NULL,
    name character varying(1024) NOT NULL,
    email character varying(1024) NOT NULL,
    facebookid character varying(1024),
    city character varying(1024),
    country character varying(1024),
    bday date,
    sex character varying(1024),
    password character varying(1024) NOT NULL,
    isactive boolean NOT NULL,
    ispublic boolean NOT NULL,
    recovering character varying(1024)
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 170 (class 1259 OID 42358)
-- Dependencies: 6 171
-- Name: users_userid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE users_userid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_userid_seq OWNER TO postgres;

--
-- TOC entry 1995 (class 0 OID 0)
-- Dependencies: 170
-- Name: users_userid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE users_userid_seq OWNED BY users.userid;


--
-- TOC entry 1996 (class 0 OID 0)
-- Dependencies: 170
-- Name: users_userid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('users_userid_seq', 24, true);


--
-- TOC entry 172 (class 1259 OID 42372)
-- Dependencies: 1935 6
-- Name: vote; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE vote (
    chatid integer NOT NULL,
    userid integer NOT NULL,
    rate integer NOT NULL,
    CONSTRAINT vote_rate CHECK ((rate = ANY (ARRAY[1, 2, 3])))
);


ALTER TABLE public.vote OWNER TO postgres;

--
-- TOC entry 1927 (class 2604 OID 42299)
-- Dependencies: 161 162 162
-- Name: attachid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY attach ALTER COLUMN attachid SET DEFAULT nextval('attach_attachid_seq'::regclass);


--
-- TOC entry 1929 (class 2604 OID 42324)
-- Dependencies: 166 165 166
-- Name: chatid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY chatroom ALTER COLUMN chatid SET DEFAULT nextval('chatroom_chatid_seq'::regclass);


--
-- TOC entry 1928 (class 2604 OID 42311)
-- Dependencies: 164 163 164
-- Name: commentid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY comment ALTER COLUMN commentid SET DEFAULT nextval('comment_commentid_seq'::regclass);


--
-- TOC entry 1932 (class 2604 OID 42350)
-- Dependencies: 168 169 169
-- Name: messageid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY message ALTER COLUMN messageid SET DEFAULT nextval('message_messageid_seq'::regclass);


--
-- TOC entry 1934 (class 2604 OID 42363)
-- Dependencies: 170 171 171
-- Name: userid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY users ALTER COLUMN userid SET DEFAULT nextval('users_userid_seq'::regclass);


--
-- TOC entry 1974 (class 0 OID 42296)
-- Dependencies: 162 1981
-- Data for Name: attach; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO attach VALUES (4, 6, '/assets/attaches/logo_universityofcoimbra_large_6317985229966880547.jpg');
INSERT INTO attach VALUES (7, 292, '');
INSERT INTO attach VALUES (8, 293, 'undefined');
INSERT INTO attach VALUES (9, 294, '');
INSERT INTO attach VALUES (10, 295, '');
INSERT INTO attach VALUES (11, 296, '');
INSERT INTO attach VALUES (12, 297, '');
INSERT INTO attach VALUES (13, 298, '');
INSERT INTO attach VALUES (14, 299, '');
INSERT INTO attach VALUES (15, 300, '');
INSERT INTO attach VALUES (16, 301, '');
INSERT INTO attach VALUES (19, 304, 'undefined');
INSERT INTO attach VALUES (20, 305, '');
INSERT INTO attach VALUES (21, 306, 'undefined');
INSERT INTO attach VALUES (22, 307, '');
INSERT INTO attach VALUES (24, 310, '/assets/attaches/report_719973068173730992.pdf');
INSERT INTO attach VALUES (25, 311, '/assets/attaches/report_1800207152420294641.pdf');
INSERT INTO attach VALUES (26, 312, '');


--
-- TOC entry 1976 (class 0 OID 42321)
-- Dependencies: 166 1981
-- Data for Name: chatroom; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO chatroom VALUES (3, 1, 'SQL ', 2, 'tecnologia', true);
INSERT INTO chatroom VALUES (5, 22, 'aaaa', NULL, 'musica', false);
INSERT INTO chatroom VALUES (6, 22, 'cenas', NULL, 'economia', false);
INSERT INTO chatroom VALUES (4, 1, 'SQL vs NoSQL', 3, 'tecnologia', true);
INSERT INTO chatroom VALUES (2, 11, 'Beck > Oasis', 2, 'musica', true);
INSERT INTO chatroom VALUES (1, 1, 'Websockets', 2, 'tecnologia', true);


--
-- TOC entry 1975 (class 0 OID 42308)
-- Dependencies: 164 1981
-- Data for Name: comment; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO comment VALUES (1, 2, 2, '2012-12-10 22:34:46.007721', 'Estou a ver!!!');
INSERT INTO comment VALUES (2, 6, 1, '2012-12-10 22:40:41.293532', 'é fascinante!');
INSERT INTO comment VALUES (3, 6, 1, '2012-12-10 22:42:59.69531', 'update');
INSERT INTO comment VALUES (4, 36, 5, '2012-12-11 23:57:58.837115', 'hey');
INSERT INTO comment VALUES (5, 36, 3, '2012-12-11 23:58:11.418939', 'gay');
INSERT INTO comment VALUES (6, 93, 1, '2012-12-12 16:47:08.573824', 'isto era para ir para a defesa...');
INSERT INTO comment VALUES (7, 93, 10, '2012-12-12 16:47:26.583774', 'Com Users chamados Dick?');
INSERT INTO comment VALUES (8, 93, 10, '2012-12-12 16:47:36.080608', 'Como é que mudo a foto?');
INSERT INTO comment VALUES (9, 93, 3, '2012-12-12 16:50:06.895824', 'registas-te com um mail decente e nós vamos buscar a foto ao gravatar, ou então entras com o facebook');
INSERT INTO comment VALUES (10, 94, 3, '2012-12-12 16:50:27.890914', 'HEY!!!!');
INSERT INTO comment VALUES (11, 94, 5, '2012-12-12 16:50:51.195678', 'Hello for you too!');
INSERT INTO comment VALUES (12, 94, 5, '2012-12-12 16:52:19.306852', 'Of course!');
INSERT INTO comment VALUES (13, 94, 5, '2012-12-12 16:55:25.44946', 'http://rfrr.no-ip.org:8080/');
INSERT INTO comment VALUES (14, 121, 3, '2012-12-12 17:06:46.105154', 'thank u baby!!!');
INSERT INTO comment VALUES (15, 121, 5, '2012-12-12 17:10:46.387105', 'um comment');
INSERT INTO comment VALUES (16, 121, 14, '2012-12-12 17:13:00.957049', 'http://tiagor13.tumblr.com/post/26231010004');
INSERT INTO comment VALUES (17, 253, 5, '2012-12-15 07:09:07.999161', 'Hey joao');
INSERT INTO comment VALUES (18, 253, 5, '2012-12-15 07:09:21.658955', 'hi');
INSERT INTO comment VALUES (19, 302, 5, '2012-12-15 07:09:38.826795', 'ola');
INSERT INTO comment VALUES (20, 302, 22, '2012-12-15 17:09:24.497053', 'a');


--
-- TOC entry 1977 (class 0 OID 42333)
-- Dependencies: 167 1981
-- Data for Name: connection; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO connection VALUES (1, 1, 'watcher');
INSERT INTO connection VALUES (1, 2, 'watcher');
INSERT INTO connection VALUES (2, 6, 'watcher');
INSERT INTO connection VALUES (2, 1, 'watcher');
INSERT INTO connection VALUES (2, 5, 'watcher');
INSERT INTO connection VALUES (1, 5, 'watcher');
INSERT INTO connection VALUES (1, 3, 'watcher');
INSERT INTO connection VALUES (3, 3, 'watcher');
INSERT INTO connection VALUES (5, 1, 'watcher');
INSERT INTO connection VALUES (6, 1, 'watcher');
INSERT INTO connection VALUES (6, 23, 'watcher');


--
-- TOC entry 1978 (class 0 OID 42347)
-- Dependencies: 169 1981
-- Data for Name: message; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO message VALUES (1, NULL, 1, '2012-12-10 21:32:49.704', 'Gostava de saber as vossas opiniões sobre esta nova tecnologia!', 1, NULL, '2012-12-10 21:32:49.704', 'public');
INSERT INTO message VALUES (2, NULL, 1, '2012-12-10 22:30:48.377827', 'Hello!', NULL, NULL, '2012-12-10 22:34:46.007721', 'post');
INSERT INTO message VALUES (7, NULL, 1, '2012-12-10 22:38:48.857', 'Recebeste a minha mensagem??', 2, NULL, '2012-12-10 22:38:48.857', 'private');
INSERT INTO message VALUES (14, NULL, 1, '2012-12-10 22:41:21.002', 'Alô!!', 2, NULL, '2012-12-10 22:41:21.002', 'private');
INSERT INTO message VALUES (15, NULL, 1, '2012-12-10 22:41:33.478', 'tás ai??', 2, NULL, '2012-12-10 22:41:33.478', 'private');
INSERT INTO message VALUES (6, NULL, 2, '2012-12-10 22:37:56.814102', 'Isto funciona em Internet Explorer!!!', NULL, NULL, '2012-12-10 22:42:59.69531', 'post');
INSERT INTO message VALUES (19, NULL, 1, '2012-12-11 15:51:48.401', 'olá!', 2, NULL, '2012-12-11 15:51:48.401', 'private');
INSERT INTO message VALUES (20, NULL, 1, '2012-12-11 15:51:52.062', 'feio!', 2, NULL, '2012-12-11 15:51:52.062', 'private');
INSERT INTO message VALUES (21, NULL, 1, '2012-12-11 15:51:56.693', 'gayzolas!', 2, NULL, '2012-12-11 15:51:56.693', 'private');
INSERT INTO message VALUES (22, NULL, 1, '2012-12-11 15:52:31.968', 'hello!', 1, NULL, '2012-12-11 15:52:31.968', 'public');
INSERT INTO message VALUES (23, NULL, 1, '2012-12-11 15:52:37.24', 'this is awesome', 1, NULL, '2012-12-11 15:52:37.24', 'public');
INSERT INTO message VALUES (24, NULL, 4, '2012-12-11 15:56:11.582788', 'is this goin to facebook?', NULL, NULL, '2012-12-11 15:56:11.582788', 'post');
INSERT INTO message VALUES (28, NULL, 3, '2012-12-11 17:34:09.157654', 'here?', NULL, NULL, '2012-12-11 17:34:09.157654', 'post');
INSERT INTO message VALUES (36, '1132836787_4690946908301', 5, '2012-12-11 23:57:16.330962', 'Esta a funcionariiii?', NULL, NULL, '2012-12-11 23:58:11.418939', 'post');
INSERT INTO message VALUES (37, NULL, 5, '2012-12-12 00:02:55.668', 'oi', 1, NULL, '2012-12-12 00:02:55.668', 'public');
INSERT INTO message VALUES (38, NULL, 5, '2012-12-12 00:02:58.136', 'godo', 1, NULL, '2012-12-12 00:02:58.136', 'public');
INSERT INTO message VALUES (39, NULL, 3, '2012-12-12 00:03:01.053', 'oi', 1, NULL, '2012-12-12 00:03:01.053', 'public');
INSERT INTO message VALUES (40, NULL, 3, '2012-12-12 00:03:02.089', 'olha', 1, NULL, '2012-12-12 00:03:02.089', 'public');
INSERT INTO message VALUES (41, NULL, 3, '2012-12-12 00:03:03.547', 'cócó', 1, NULL, '2012-12-12 00:03:03.547', 'public');
INSERT INTO message VALUES (42, NULL, 3, '2012-12-12 00:03:14.093', 'tenho pessoal a dizer-me que não consegue entrar com o facebook', 1, NULL, '2012-12-12 00:03:14.093', 'public');
INSERT INTO message VALUES (43, NULL, 3, '2012-12-12 00:03:15.552', ':x', 1, NULL, '2012-12-12 00:03:15.552', 'public');
INSERT INTO message VALUES (44, NULL, 5, '2012-12-12 00:03:43.812', 'a mim deu-me bem', 1, NULL, '2012-12-12 00:03:43.812', 'public');
INSERT INTO message VALUES (45, NULL, 3, '2012-12-12 00:03:49.536', 'n sei...', 1, NULL, '2012-12-12 00:03:49.536', 'public');
INSERT INTO message VALUES (46, NULL, 5, '2012-12-12 00:03:51.151', 'mas eu ja tinha aplicação aceite', 1, NULL, '2012-12-12 00:03:51.151', 'public');
INSERT INTO message VALUES (47, NULL, 5, '2012-12-12 00:04:02.796', 'tenta entrar com algum utilizador que nao tenho a aplicação', 1, NULL, '2012-12-12 00:04:02.796', 'public');
INSERT INTO message VALUES (48, NULL, 3, '2012-12-12 00:04:59.08', 'a cena é que acho que não é isso que está a dar cócó', 1, NULL, '2012-12-12 00:04:59.08', 'public');
INSERT INTO message VALUES (49, NULL, 3, '2012-12-12 00:05:05.202', 'pera aí', 1, NULL, '2012-12-12 00:05:05.202', 'public');
INSERT INTO message VALUES (50, NULL, 3, '2012-12-12 00:05:11.825', 'vou tentar entrar como eu', 1, NULL, '2012-12-12 00:05:11.825', 'public');
INSERT INTO message VALUES (51, NULL, 1, '2012-12-12 00:05:27.421', 'here', 1, NULL, '2012-12-12 00:05:27.421', 'public');
INSERT INTO message VALUES (52, NULL, 5, '2012-12-12 00:05:32.436', 'funcionou?', 1, NULL, '2012-12-12 00:05:32.436', 'public');
INSERT INTO message VALUES (53, NULL, 1, '2012-12-12 00:05:53.665', 'como eu sim', 1, NULL, '2012-12-12 00:05:53.665', 'public');
INSERT INTO message VALUES (54, NULL, 1, '2012-12-12 00:06:14.373', 'mas ele tá a dizer-me que o erro é da página', 1, NULL, '2012-12-12 00:06:14.373', 'public');
INSERT INTO message VALUES (55, NULL, 5, '2012-12-12 00:06:29.777', 'mas tu apagaste a aplicação antes?', 1, NULL, '2012-12-12 00:06:29.777', 'public');
INSERT INTO message VALUES (56, NULL, 5, '2012-12-12 00:06:40.929', 'para aparecer o pedido', 1, NULL, '2012-12-12 00:06:40.929', 'public');
INSERT INTO message VALUES (57, NULL, 1, '2012-12-12 00:08:19.742', 'pera', 1, NULL, '2012-12-12 00:08:19.742', 'public');
INSERT INTO message VALUES (58, NULL, 1, '2012-12-12 00:08:22.417', 'que ', 1, NULL, '2012-12-12 00:08:22.417', 'public');
INSERT INTO message VALUES (59, NULL, 1, '2012-12-12 00:08:27.39', 'pode ser do redirect', 1, NULL, '2012-12-12 00:08:27.39', 'public');
INSERT INTO message VALUES (60, NULL, 1, '2012-12-12 00:09:45.18', 'ficou ali uma cena fodida qd disseste para aparecer o pedido', 1, NULL, '2012-12-12 00:09:45.18', 'public');
INSERT INTO message VALUES (61, NULL, 1, '2012-12-12 00:11:08.442', 'tás aí gordo??', 1, NULL, '2012-12-12 00:11:08.442', 'public');
INSERT INTO message VALUES (62, NULL, 5, '2012-12-12 00:11:36.766', 'oi gordo', 1, NULL, '2012-12-12 00:11:36.766', 'public');
INSERT INTO message VALUES (63, NULL, 1, '2012-12-12 00:12:11.994', 'viste o que te falei?', 1, NULL, '2012-12-12 00:12:11.994', 'public');
INSERT INTO message VALUES (64, NULL, 5, '2012-12-12 00:12:20.384', 'ya', 1, NULL, '2012-12-12 00:12:20.384', 'public');
INSERT INTO message VALUES (65, NULL, 5, '2012-12-12 00:12:25.488', 'eu reparei', 1, NULL, '2012-12-12 00:12:25.488', 'public');
INSERT INTO message VALUES (66, NULL, 1, '2012-12-12 00:12:26.087', 'pronto', 1, NULL, '2012-12-12 00:12:26.087', 'public');
INSERT INTO message VALUES (67, NULL, 1, '2012-12-12 00:12:29.279', 'tipo', 1, NULL, '2012-12-12 00:12:29.279', 'public');
INSERT INTO message VALUES (68, NULL, 1, '2012-12-12 00:12:39.868', 'o redirect do facebook n tá a funcar', 1, NULL, '2012-12-12 00:12:39.868', 'public');
INSERT INTO message VALUES (69, NULL, 1, '2012-12-12 00:12:42.711', 'faz uma coisa', 1, NULL, '2012-12-12 00:12:42.711', 'public');
INSERT INTO message VALUES (70, NULL, 5, '2012-12-12 00:12:45.199', 'tenho que meter o chat com avisos', 1, NULL, '2012-12-12 00:12:45.199', 'public');
INSERT INTO message VALUES (71, NULL, 5, '2012-12-12 00:12:52.528', 'não esta? testaste?', 1, NULL, '2012-12-12 00:12:52.528', 'public');
INSERT INTO message VALUES (72, NULL, 1, '2012-12-12 00:13:06.7', 'https://developers.facebook.com/apps/268297886625833/roles', 1, NULL, '2012-12-12 00:13:06.7', 'public');
INSERT INTO message VALUES (73, NULL, 1, '2012-12-12 00:13:14.76', 'com quem está já autorizado sim', 1, NULL, '2012-12-12 00:13:14.76', 'public');
INSERT INTO message VALUES (74, NULL, 1, '2012-12-12 00:13:19.787', 'com quem não está', 1, NULL, '2012-12-12 00:13:19.787', 'public');
INSERT INTO message VALUES (75, NULL, 1, '2012-12-12 00:13:25.974', 'ele n sai da mesma página sequer', 1, NULL, '2012-12-12 00:13:25.974', 'public');
INSERT INTO message VALUES (76, NULL, 5, '2012-12-12 00:13:28.731', 'testa com quem nao esta', 1, NULL, '2012-12-12 00:13:28.731', 'public');
INSERT INTO message VALUES (77, NULL, 5, '2012-12-12 00:13:31.627', '&gt;.&lt;', 1, NULL, '2012-12-12 00:13:31.627', 'public');
INSERT INTO message VALUES (78, NULL, 1, '2012-12-12 00:13:37.397', 'mas n me dá nenhum erro do lado do servidor', 1, NULL, '2012-12-12 00:13:37.397', 'public');
INSERT INTO message VALUES (79, NULL, 5, '2012-12-12 00:13:41.37', 'isso é estranho, ele faz o redirect', 1, NULL, '2012-12-12 00:13:41.37', 'public');
INSERT INTO message VALUES (80, NULL, 1, '2012-12-12 00:13:43.068', 'vai ao site que eu te mostrei', 1, NULL, '2012-12-12 00:13:43.068', 'public');
INSERT INTO message VALUES (81, NULL, 5, '2012-12-12 00:13:52.944', 'nem login daria para fazer', 1, NULL, '2012-12-12 00:13:52.944', 'public');
INSERT INTO message VALUES (82, NULL, 1, '2012-12-12 00:14:12.136', 'e muda para um dos dummy', 1, NULL, '2012-12-12 00:14:12.136', 'public');
INSERT INTO message VALUES (83, NULL, 1, '2012-12-12 00:14:15.873', 'e vê o que acontece', 1, NULL, '2012-12-12 00:14:15.873', 'public');
INSERT INTO message VALUES (84, NULL, 1, '2012-12-12 00:17:10.778', 'boa cena o login normal tb n está a funcionar', 1, NULL, '2012-12-12 00:17:10.778', 'public');
INSERT INTO message VALUES (85, '100004890152468_100973670075638', 7, '2012-12-12 00:36:36.662826', 'facebook?', NULL, NULL, '2012-12-12 00:36:36.662826', 'post');
INSERT INTO message VALUES (86, NULL, 8, '2012-12-12 01:13:09.06', 'i &lt;3 u', 1, '2012-12-12 01:13:40.006046', '2012-12-12 01:13:09.06', 'private');
INSERT INTO message VALUES (87, NULL, 1, '2012-12-12 01:13:42.965', 'ihihi', 8, '2012-12-12 01:13:46.392233', '2012-12-12 01:13:42.965', 'private');
INSERT INTO message VALUES (88, NULL, 1, '2012-12-12 01:13:46.824', 'i &lt;3 u too', 8, '2012-12-12 01:13:55.886013', '2012-12-12 01:13:46.824', 'private');
INSERT INTO message VALUES (89, NULL, 8, '2012-12-12 01:13:54.033', 'ohsis smosis', 1, '2012-12-12 01:14:43.322346', '2012-12-12 01:13:54.033', 'private');
INSERT INTO message VALUES (90, NULL, 1, '2012-12-12 01:14:46.743', 'ihihi', 8, '2012-12-12 01:15:03.780219', '2012-12-12 01:14:46.743', 'private');
INSERT INTO message VALUES (91, NULL, 8, '2012-12-12 01:15:11.899', 'silly punzito', 1, '2012-12-12 16:40:18.413644', '2012-12-12 01:15:11.899', 'private');
INSERT INTO message VALUES (109, NULL, 8, '2012-12-12 17:01:54.167', 'bitch', 1, '2012-12-12 17:09:27.492556', '2012-12-12 17:01:54.167', 'private');
INSERT INTO message VALUES (93, NULL, 10, '2012-12-12 16:46:24.299364', 'Chupa aqui pq sei que gostas', NULL, NULL, '2012-12-12 16:50:06.895824', 'post');
INSERT INTO message VALUES (92, NULL, 1, '2012-12-12 16:42:07.032', 'Im no such thing!', 8, '2012-12-12 16:50:26.352113', '2012-12-12 16:42:07.032', 'private');
INSERT INTO message VALUES (146, NULL, 3, '2012-12-12 17:10:32.546', 'xD', 14, '2012-12-12 17:11:02.379218', '2012-12-12 17:10:32.546', 'private');
INSERT INTO message VALUES (95, NULL, 8, '2012-12-12 16:50:34.059', 'you are!!!
', 1, '2012-12-12 16:52:51.177988', '2012-12-12 16:50:34.059', 'private');
INSERT INTO message VALUES (94, '583107809_10151175226252810', 11, '2012-12-12 16:49:39.355841', 'HEY, Everyone!', NULL, NULL, '2012-12-12 16:55:25.44946', 'post');
INSERT INTO message VALUES (96, NULL, 1, '2012-12-12 16:52:55.44', 'ih ih ih', 8, '2012-12-12 16:57:25.10856', '2012-12-12 16:52:55.44', 'private');
INSERT INTO message VALUES (97, NULL, 11, '2012-12-12 16:57:25.803', 'Its true!', 2, NULL, '2012-12-12 16:57:25.803', 'public');
INSERT INTO message VALUES (98, NULL, 5, '2012-12-12 16:58:25.685', 'Oasis &lt; Beck', 2, NULL, '2012-12-12 16:58:25.685', 'public');
INSERT INTO message VALUES (99, NULL, 5, '2012-12-12 16:58:28.486', 'ops', 2, NULL, '2012-12-12 16:58:28.486', 'public');
INSERT INTO message VALUES (100, NULL, 5, '2012-12-12 16:58:31.264', 'that''s the same shit', 2, NULL, '2012-12-12 16:58:31.264', 'public');
INSERT INTO message VALUES (101, NULL, 1, '2012-12-12 16:58:35.169', 'they both suck!', 2, NULL, '2012-12-12 16:58:35.169', 'public');
INSERT INTO message VALUES (102, NULL, 5, '2012-12-12 16:58:35.754', 'Oasis &gt; Beck', 2, NULL, '2012-12-12 16:58:35.754', 'public');
INSERT INTO message VALUES (103, NULL, 11, '2012-12-12 16:58:42.434', 'false', 2, NULL, '2012-12-12 16:58:42.434', 'public');
INSERT INTO message VALUES (104, NULL, 11, '2012-12-12 16:58:55.853', 'they are both good. but one is best!', 2, NULL, '2012-12-12 16:58:55.853', 'public');
INSERT INTO message VALUES (105, NULL, 5, '2012-12-12 16:58:56.515', 'c''est tres true', 2, NULL, '2012-12-12 16:58:56.515', 'public');
INSERT INTO message VALUES (106, NULL, 5, '2012-12-12 17:00:34.456', 'Beck makes no sense', 2, NULL, '2012-12-12 17:00:34.456', 'public');
INSERT INTO message VALUES (107, NULL, 1, '2012-12-12 17:01:18.675', 'they both suck', 2, NULL, '2012-12-12 17:01:18.675', 'public');
INSERT INTO message VALUES (108, NULL, 1, '2012-12-12 17:01:44.051', 'and i''de rather shoot myself to listen to them', 2, NULL, '2012-12-12 17:01:44.051', 'public');
INSERT INTO message VALUES (110, NULL, 11, '2012-12-12 17:02:42.292', 'what ever', 2, NULL, '2012-12-12 17:02:42.292', 'public');
INSERT INTO message VALUES (111, NULL, 11, '2012-12-12 17:02:53.782', 'who is better then?', 2, NULL, '2012-12-12 17:02:53.782', 'public');
INSERT INTO message VALUES (112, NULL, 5, '2012-12-12 17:03:12.64', 'Oasis', 2, NULL, '2012-12-12 17:03:12.64', 'public');
INSERT INTO message VALUES (113, NULL, 5, '2012-12-12 17:03:25.508', 'Im gonna put some Beck so he shoots himself', 2, NULL, '2012-12-12 17:03:25.508', 'public');
INSERT INTO message VALUES (114, NULL, 1, '2012-12-12 17:03:29.016', 'my mom', 2, NULL, '2012-12-12 17:03:29.016', 'public');
INSERT INTO message VALUES (115, NULL, 1, '2012-12-12 17:03:35.011', 'she is waaaaaaaaaaaaaay better', 2, NULL, '2012-12-12 17:03:35.011', 'public');
INSERT INTO message VALUES (116, NULL, 5, '2012-12-12 17:03:57.237', 'i know ;)', 2, NULL, '2012-12-12 17:03:57.237', 'public');
INSERT INTO message VALUES (117, NULL, 11, '2012-12-12 17:04:16.849', 'ewwww', 2, NULL, '2012-12-12 17:04:16.849', 'public');
INSERT INTO message VALUES (118, NULL, 11, '2012-12-12 17:04:22.874', 'thats not cool', 2, NULL, '2012-12-12 17:04:22.874', 'public');
INSERT INTO message VALUES (119, NULL, 11, '2012-12-12 17:05:06.685', 'whatever Joao, you probs like Backstreet Boys', 2, NULL, '2012-12-12 17:05:06.685', 'public');
INSERT INTO message VALUES (120, NULL, 1, '2012-12-12 17:05:35.483', '&lt;3', 2, NULL, '2012-12-12 17:05:35.483', 'public');
INSERT INTO message VALUES (145, NULL, 3, '2012-12-12 17:10:29.943', 'não te aparecia nada pq n tinhas amigos!', 14, '2012-12-12 17:11:02.484694', '2012-12-12 17:10:29.943', 'private');
INSERT INTO message VALUES (122, NULL, 11, '2012-12-12 17:06:14.976', 'anyway', 2, NULL, '2012-12-12 17:06:14.976', 'public');
INSERT INTO message VALUES (123, NULL, 11, '2012-12-12 17:06:31.809', 'Beck is still better. He has different sounds than oasis', 2, NULL, '2012-12-12 17:06:31.809', 'public');
INSERT INTO message VALUES (124, NULL, 5, '2012-12-12 17:06:45.421', 'no he doesn''t', 2, NULL, '2012-12-12 17:06:45.421', 'public');
INSERT INTO message VALUES (151, NULL, 5, '2012-12-12 17:11:41.381', 'shame on you', 2, NULL, '2012-12-12 17:11:41.381', 'public');
INSERT INTO message VALUES (125, NULL, 11, '2012-12-12 17:07:07.977', 'Yes! you just dont understand his complex lyrics', 2, NULL, '2012-12-12 17:07:07.977', 'public');
INSERT INTO message VALUES (126, NULL, 1, '2012-12-12 17:07:23.397', 'and justin bieber too', 2, NULL, '2012-12-12 17:07:23.397', 'public');
INSERT INTO message VALUES (127, NULL, 5, '2012-12-12 17:07:26.826', 'hahah', 2, NULL, '2012-12-12 17:07:26.826', 'public');
INSERT INTO message VALUES (128, NULL, 11, '2012-12-12 17:07:28.676', 'you would', 2, NULL, '2012-12-12 17:07:28.676', 'public');
INSERT INTO message VALUES (129, NULL, 5, '2012-12-12 17:07:29.741', 'SHOW YOU OFF!', 2, NULL, '2012-12-12 17:07:29.741', 'public');
INSERT INTO message VALUES (130, NULL, 5, '2012-12-12 17:07:36.658', 'TONIGHT I WANNA SHOW YOU OFF!', 2, NULL, '2012-12-12 17:07:36.658', 'public');
INSERT INTO message VALUES (131, NULL, 1, '2012-12-12 17:07:45.826', 'you know whu else haz complex lyrics?', 2, NULL, '2012-12-12 17:07:45.826', 'public');
INSERT INTO message VALUES (132, NULL, 1, '2012-12-12 17:07:49.176', 'my mom', 2, NULL, '2012-12-12 17:07:49.176', 'public');
INSERT INTO message VALUES (133, NULL, 1, '2012-12-12 17:07:49.953', 'xD', 2, NULL, '2012-12-12 17:07:49.953', 'public');
INSERT INTO message VALUES (134, NULL, 1, '2012-12-12 17:07:54.697', 'EH EH EHE', 2, NULL, '2012-12-12 17:07:54.697', 'public');
INSERT INTO message VALUES (135, NULL, 5, '2012-12-12 17:08:00.602', 'yes she does', 2, NULL, '2012-12-12 17:08:00.602', 'public');
INSERT INTO message VALUES (136, NULL, 5, '2012-12-12 17:08:03.402', ';)', 2, NULL, '2012-12-12 17:08:03.402', 'public');
INSERT INTO message VALUES (137, NULL, 11, '2012-12-12 17:08:06.079', 'lolz', 2, NULL, '2012-12-12 17:08:06.079', 'public');
INSERT INTO message VALUES (138, NULL, 5, '2012-12-12 17:08:07.378', 'if you know what I mean', 2, NULL, '2012-12-12 17:08:07.378', 'public');
INSERT INTO message VALUES (139, NULL, 5, '2012-12-12 17:08:08.635', ';)', 2, NULL, '2012-12-12 17:08:08.635', 'public');
INSERT INTO message VALUES (140, NULL, 11, '2012-12-12 17:08:14.08', 'we get it', 2, NULL, '2012-12-12 17:08:14.08', 'public');
INSERT INTO message VALUES (141, NULL, 5, '2012-12-12 17:08:16.617', ';)', 2, NULL, '2012-12-12 17:08:16.617', 'public');
INSERT INTO message VALUES (142, NULL, 11, '2012-12-12 17:08:18.828', 'its done', 2, NULL, '2012-12-12 17:08:18.828', 'public');
INSERT INTO message VALUES (144, NULL, 3, '2012-12-12 17:10:19.743', 'agora já tens aqui coisas!', 14, '2012-12-12 17:11:02.497795', '2012-12-12 17:10:19.743', 'private');
INSERT INTO message VALUES (150, NULL, 5, '2012-12-12 17:11:36.435', 'your discussion makes no sense', 2, NULL, '2012-12-12 17:11:36.435', 'public');
INSERT INTO message VALUES (153, NULL, 1, '2012-12-12 17:12:07.384', 'agora já cá tens coisas!', 13, '2012-12-12 17:12:28.574151', '2012-12-12 17:12:07.384', 'private');
INSERT INTO message VALUES (154, NULL, 11, '2012-12-12 17:12:39.645', 'whatever', 2, NULL, '2012-12-12 17:12:39.645', 'public');
INSERT INTO message VALUES (121, '100000400120398_493739837316016', 14, '2012-12-12 17:05:42.664505', 'Happy thoughts ! ', NULL, NULL, '2012-12-12 17:13:00.957049', 'post');
INSERT INTO message VALUES (159, NULL, 13, '2012-12-12 17:13:42.983', 'é confuso estar a falar em dois sítios... não vi essa opção', 1, '2012-12-12 17:15:10.130195', '2012-12-12 17:13:42.983', 'private');
INSERT INTO message VALUES (155, NULL, 13, '2012-12-12 17:12:39.953', 'cenas?', 1, '2012-12-12 17:15:10.202436', '2012-12-12 17:12:39.953', 'private');
INSERT INTO message VALUES (160, NULL, 1, '2012-12-12 17:15:15.776', 'xD', 13, '2012-12-12 17:16:01.412727', '2012-12-12 17:15:15.776', 'private');
INSERT INTO message VALUES (158, NULL, 1, '2012-12-12 17:12:57.925', 'e escolhes enviar mensagem', 13, '2012-12-12 17:16:01.450148', '2012-12-12 17:12:57.925', 'private');
INSERT INTO message VALUES (157, NULL, 1, '2012-12-12 17:12:53.077', 'num post meu', 13, '2012-12-12 17:16:01.458797', '2012-12-12 17:12:53.077', 'private');
INSERT INTO message VALUES (156, NULL, 1, '2012-12-12 17:12:49.989', 'carregas na minha foto', 13, '2012-12-12 17:16:01.469377', '2012-12-12 17:12:49.989', 'private');
INSERT INTO message VALUES (152, NULL, 14, '2012-12-12 17:11:45.606', 'nao devia aparecer um botaozinho pra voltar pra tras ?', 3, '2012-12-12 17:16:11.191067', '2012-12-12 17:11:45.606', 'private');
INSERT INTO message VALUES (149, NULL, 14, '2012-12-12 17:11:35.276', 'mas', 3, '2012-12-12 17:16:11.239074', '2012-12-12 17:11:35.276', 'private');
INSERT INTO message VALUES (147, NULL, 14, '2012-12-12 17:11:08.84', 'aaaaahhhh', 3, '2012-12-12 17:16:11.268488', '2012-12-12 17:11:08.84', 'private');
INSERT INTO message VALUES (143, NULL, 1, '2012-12-12 17:09:30.415', 'why?', 8, '2012-12-12 17:54:16.257859', '2012-12-12 17:09:30.415', 'private');
INSERT INTO message VALUES (162, NULL, 11, '2012-12-12 17:15:26.616', 'my claim is that my music taste is better', 2, NULL, '2012-12-12 17:15:26.616', 'public');
INSERT INTO message VALUES (163, NULL, 11, '2012-12-12 17:15:35.808', 'oasis is baby stuff', 2, NULL, '2012-12-12 17:15:35.808', 'public');
INSERT INTO message VALUES (164, NULL, 5, '2012-12-12 17:15:59.156', 'which is a false claim', 2, NULL, '2012-12-12 17:15:59.156', 'public');
INSERT INTO message VALUES (161, NULL, 1, '2012-12-12 17:15:26.018', 'fala só por aqui que é mais privado', 13, '2012-12-12 17:16:01.155262', '2012-12-12 17:15:26.018', 'private');
INSERT INTO message VALUES (165, NULL, 5, '2012-12-12 17:16:06.677', 'therefore you statment is invalid', 2, NULL, '2012-12-12 17:16:06.677', 'public');
INSERT INTO message VALUES (148, NULL, 14, '2012-12-12 17:11:27.281', 'ta bem
mas mesmo assim
devia existir um botaozinho pra voltar pra tras', 3, '2012-12-12 17:16:11.253137', '2012-12-12 17:11:27.281', 'private');
INSERT INTO message VALUES (166, NULL, 3, '2012-12-12 17:16:18.323', 'para que?', 14, NULL, '2012-12-12 17:16:18.323', 'private');
INSERT INTO message VALUES (167, NULL, 5, '2012-12-12 17:16:21.761', 'hence the fact that oasis is better', 2, NULL, '2012-12-12 17:16:21.761', 'public');
INSERT INTO message VALUES (168, NULL, 11, '2012-12-12 17:16:39.512', 'that is falicy!', 2, NULL, '2012-12-12 17:16:39.512', 'public');
INSERT INTO message VALUES (169, NULL, 3, '2012-12-12 17:16:46.843', 'tens o b', 14, NULL, '2012-12-12 17:16:46.843', 'private');
INSERT INTO message VALUES (170, NULL, 3, '2012-12-12 17:17:01.83', 'carrega no facepalm
que voltas para a principal', 14, NULL, '2012-12-12 17:17:01.83', 'private');
INSERT INTO message VALUES (171, NULL, 3, '2012-12-12 17:17:09.735', 'carrega no facepalm', 14, NULL, '2012-12-12 17:17:09.735', 'private');
INSERT INTO message VALUES (172, NULL, 5, '2012-12-12 17:17:11.086', 'your face is a fallacy', 2, NULL, '2012-12-12 17:17:11.086', 'public');
INSERT INTO message VALUES (173, NULL, 3, '2012-12-12 17:17:18.357', 'e voltas para
a principal', 14, NULL, '2012-12-12 17:17:18.357', 'private');
INSERT INTO message VALUES (174, NULL, 3, '2012-12-12 17:17:35.96', 'e voltas para a página principal', 14, NULL, '2012-12-12 17:17:35.96', 'private');
INSERT INTO message VALUES (175, NULL, 11, '2012-12-12 17:18:25.412', 'if by that, you mean beck is better than oasis . then yes', 2, NULL, '2012-12-12 17:18:25.412', 'public');
INSERT INTO message VALUES (176, NULL, 5, '2012-12-12 17:19:21.751', 'by that I mean that your statement make as much sense as beck''s lyrics', 2, NULL, '2012-12-12 17:19:21.751', 'public');
INSERT INTO message VALUES (177, NULL, 5, '2012-12-12 17:19:24.727', '(none)', 2, NULL, '2012-12-12 17:19:24.727', 'public');
INSERT INTO message VALUES (178, NULL, 1, '2012-12-12 17:19:55.493', 'http://endlessvideo.com/watch?v=uE-1RPDqJAY', 2, NULL, '2012-12-12 17:19:55.493', 'public');
INSERT INTO message VALUES (179, NULL, 11, '2012-12-12 17:22:42.933', 'its just above your head', 2, NULL, '2012-12-12 17:22:42.933', 'public');
INSERT INTO message VALUES (181, NULL, 3, '2012-12-12 17:32:01.384', 'aa', 5, '2012-12-12 17:32:01.728535', '2012-12-12 17:32:01.384', 'private');
INSERT INTO message VALUES (180, NULL, 3, '2012-12-12 17:31:59.55', 'a', 5, '2012-12-12 17:32:01.744174', '2012-12-12 17:31:59.55', 'private');
INSERT INTO message VALUES (190, NULL, 5, '2012-12-12 17:32:51.719', 'GOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOORDO!!!', 2, NULL, '2012-12-12 17:32:51.719', 'public');
INSERT INTO message VALUES (191, NULL, 5, '2012-12-12 17:33:06.314', 'awfasdf', 2, NULL, '2012-12-12 17:33:06.314', 'public');
INSERT INTO message VALUES (192, NULL, 5, '2012-12-12 17:33:06.825', 'as', 2, NULL, '2012-12-12 17:33:06.825', 'public');
INSERT INTO message VALUES (193, NULL, 5, '2012-12-12 17:33:06.991', 'df', 2, NULL, '2012-12-12 17:33:06.991', 'public');
INSERT INTO message VALUES (194, NULL, 5, '2012-12-12 17:33:07.18', 'asdf', 2, NULL, '2012-12-12 17:33:07.18', 'public');
INSERT INTO message VALUES (195, NULL, 5, '2012-12-12 17:33:07.816', 'f', 2, NULL, '2012-12-12 17:33:07.816', 'public');
INSERT INTO message VALUES (196, NULL, 5, '2012-12-12 17:33:10.364', 'asdfsaddddddddddaaad', 2, NULL, '2012-12-12 17:33:10.364', 'public');
INSERT INTO message VALUES (197, NULL, 5, '2012-12-12 17:33:10.783', 'd', 2, NULL, '2012-12-12 17:33:10.783', 'public');
INSERT INTO message VALUES (198, NULL, 5, '2012-12-12 17:33:10.964', 'd', 2, NULL, '2012-12-12 17:33:10.964', 'public');
INSERT INTO message VALUES (199, NULL, 5, '2012-12-12 17:33:11.159', 'd', 2, NULL, '2012-12-12 17:33:11.159', 'public');
INSERT INTO message VALUES (201, NULL, 5, '2012-12-12 17:33:11.343', 'd', 2, NULL, '2012-12-12 17:33:11.343', 'public');
INSERT INTO message VALUES (202, NULL, 5, '2012-12-12 17:33:11.536', 'd', 2, NULL, '2012-12-12 17:33:11.536', 'public');
INSERT INTO message VALUES (203, NULL, 5, '2012-12-12 17:33:11.721', 'd', 2, NULL, '2012-12-12 17:33:11.721', 'public');
INSERT INTO message VALUES (204, NULL, 5, '2012-12-12 17:33:11.915', 'd', 2, NULL, '2012-12-12 17:33:11.915', 'public');
INSERT INTO message VALUES (205, NULL, 5, '2012-12-12 17:33:12.1', 'd', 2, NULL, '2012-12-12 17:33:12.1', 'public');
INSERT INTO message VALUES (206, NULL, 5, '2012-12-12 17:33:12.309', 'd', 2, NULL, '2012-12-12 17:33:12.309', 'public');
INSERT INTO message VALUES (207, NULL, 5, '2012-12-12 17:33:12.506', 'd', 2, NULL, '2012-12-12 17:33:12.506', 'public');
INSERT INTO message VALUES (208, NULL, 5, '2012-12-12 17:33:12.696', 'd', 2, NULL, '2012-12-12 17:33:12.696', 'public');
INSERT INTO message VALUES (209, NULL, 5, '2012-12-12 17:33:12.904', 'd', 2, NULL, '2012-12-12 17:33:12.904', 'public');
INSERT INTO message VALUES (210, NULL, 5, '2012-12-12 17:33:13.116', 'd', 2, NULL, '2012-12-12 17:33:13.116', 'public');
INSERT INTO message VALUES (211, NULL, 5, '2012-12-12 17:33:13.305', 'd', 2, NULL, '2012-12-12 17:33:13.305', 'public');
INSERT INTO message VALUES (212, NULL, 5, '2012-12-12 17:33:13.513', 'd', 2, NULL, '2012-12-12 17:33:13.513', 'public');
INSERT INTO message VALUES (213, NULL, 5, '2012-12-12 17:33:13.696', 'd', 2, NULL, '2012-12-12 17:33:13.696', 'public');
INSERT INTO message VALUES (214, NULL, 5, '2012-12-12 17:33:13.907', 'd', 2, NULL, '2012-12-12 17:33:13.907', 'public');
INSERT INTO message VALUES (215, NULL, 5, '2012-12-12 17:33:14.1', 'd', 2, NULL, '2012-12-12 17:33:14.1', 'public');
INSERT INTO message VALUES (216, NULL, 5, '2012-12-12 17:33:14.301', 'd', 2, NULL, '2012-12-12 17:33:14.301', 'public');
INSERT INTO message VALUES (217, NULL, 5, '2012-12-12 17:33:14.496', 'd', 2, NULL, '2012-12-12 17:33:14.496', 'public');
INSERT INTO message VALUES (218, NULL, 5, '2012-12-12 17:33:14.697', 'd', 2, NULL, '2012-12-12 17:33:14.697', 'public');
INSERT INTO message VALUES (219, NULL, 5, '2012-12-12 17:33:14.891', 'd', 2, NULL, '2012-12-12 17:33:14.891', 'public');
INSERT INTO message VALUES (220, NULL, 5, '2012-12-12 17:33:15.106', 'd', 2, NULL, '2012-12-12 17:33:15.106', 'public');
INSERT INTO message VALUES (221, NULL, 5, '2012-12-12 17:33:15.304', 'd', 2, NULL, '2012-12-12 17:33:15.304', 'public');
INSERT INTO message VALUES (222, NULL, 5, '2012-12-12 17:33:15.512', 'd', 2, NULL, '2012-12-12 17:33:15.512', 'public');
INSERT INTO message VALUES (223, NULL, 5, '2012-12-12 17:33:15.712', 'd', 2, NULL, '2012-12-12 17:33:15.712', 'public');
INSERT INTO message VALUES (224, NULL, 5, '2012-12-12 17:33:15.921', 'd', 2, NULL, '2012-12-12 17:33:15.921', 'public');
INSERT INTO message VALUES (225, NULL, 5, '2012-12-12 17:33:16.13', 'd', 2, NULL, '2012-12-12 17:33:16.13', 'public');
INSERT INTO message VALUES (226, NULL, 5, '2012-12-12 17:33:16.352', 'd', 2, NULL, '2012-12-12 17:33:16.352', 'public');
INSERT INTO message VALUES (227, NULL, 5, '2012-12-12 17:33:19.37', 'dksakldfasdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd', 2, NULL, '2012-12-12 17:33:19.37', 'public');
INSERT INTO message VALUES (228, NULL, 5, '2012-12-12 17:33:20.106', '4ds', 2, NULL, '2012-12-12 17:33:20.106', 'public');
INSERT INTO message VALUES (229, NULL, 5, '2012-12-12 17:33:20.35', 'fsd', 2, NULL, '2012-12-12 17:33:20.35', 'public');
INSERT INTO message VALUES (230, NULL, 5, '2012-12-12 17:33:20.566', 'fs', 2, NULL, '2012-12-12 17:33:20.566', 'public');
INSERT INTO message VALUES (231, NULL, 5, '2012-12-12 17:33:20.804', 'df', 2, NULL, '2012-12-12 17:33:20.804', 'public');
INSERT INTO message VALUES (232, NULL, 5, '2012-12-12 17:33:21.042', 'd', 2, NULL, '2012-12-12 17:33:21.042', 'public');
INSERT INTO message VALUES (233, NULL, 5, '2012-12-12 17:33:22.385', 'ddddddGOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOORDO!!!', 2, NULL, '2012-12-12 17:33:22.385', 'public');
INSERT INTO message VALUES (234, NULL, 5, '2012-12-12 17:33:22.817', 'GOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOORDO!!!', 2, NULL, '2012-12-12 17:33:22.817', 'public');
INSERT INTO message VALUES (235, NULL, 5, '2012-12-12 17:33:23.236', 'GOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOORDO!!!', 2, NULL, '2012-12-12 17:33:23.236', 'public');
INSERT INTO message VALUES (236, NULL, 5, '2012-12-12 17:33:23.597', 'GOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOORDO!!!', 2, NULL, '2012-12-12 17:33:23.597', 'public');
INSERT INTO message VALUES (237, NULL, 5, '2012-12-12 17:33:23.993', 'sd', 2, NULL, '2012-12-12 17:33:23.993', 'public');
INSERT INTO message VALUES (238, NULL, 5, '2012-12-12 17:33:24.235', 'fs', 2, NULL, '2012-12-12 17:33:24.235', 'public');
INSERT INTO message VALUES (239, NULL, 5, '2012-12-12 17:33:24.545', 'd', 2, NULL, '2012-12-12 17:33:24.545', 'public');
INSERT INTO message VALUES (240, NULL, 5, '2012-12-12 17:33:24.973', 'sdf', 2, NULL, '2012-12-12 17:33:24.973', 'public');
INSERT INTO message VALUES (241, NULL, 5, '2012-12-12 17:33:25.813', 'sdgsdfgsdfg', 2, NULL, '2012-12-12 17:33:25.813', 'public');
INSERT INTO message VALUES (200, NULL, 3, '2012-12-12 17:33:11.24', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 5, '2012-12-12 17:33:47.909517', '2012-12-12 17:33:11.24', 'private');
INSERT INTO message VALUES (189, NULL, 3, '2012-12-12 17:32:43.604', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 5, '2012-12-12 17:33:47.937921', '2012-12-12 17:32:43.604', 'private');
INSERT INTO message VALUES (188, NULL, 3, '2012-12-12 17:32:39.698', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 5, '2012-12-12 17:33:47.948411', '2012-12-12 17:32:39.698', 'private');
INSERT INTO message VALUES (187, NULL, 3, '2012-12-12 17:32:34.147', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 5, '2012-12-12 17:33:47.955744', '2012-12-12 17:32:34.147', 'private');
INSERT INTO message VALUES (185, NULL, 3, '2012-12-12 17:32:16.33', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 5, '2012-12-12 17:33:47.972433', '2012-12-12 17:32:16.33', 'private');
INSERT INTO message VALUES (184, NULL, 3, '2012-12-12 17:32:13.607', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 5, '2012-12-12 17:33:47.977908', '2012-12-12 17:32:13.607', 'private');
INSERT INTO message VALUES (183, NULL, 3, '2012-12-12 17:32:10.366', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 5, '2012-12-12 17:33:47.989646', '2012-12-12 17:32:10.366', 'private');
INSERT INTO message VALUES (182, NULL, 3, '2012-12-12 17:32:05.702', 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 5, '2012-12-12 17:33:48.003833', '2012-12-12 17:32:05.702', 'private');
INSERT INTO message VALUES (186, NULL, 5, '2012-12-12 17:32:16.856', 'GOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOORDO!!!', 3, '2012-12-12 17:33:41.693356', '2012-12-12 17:32:16.856', 'private');
INSERT INTO message VALUES (242, NULL, 8, '2012-12-12 17:54:22.947', 'cuz i say so', 1, '2012-12-12 18:20:15.720793', '2012-12-12 17:54:22.947', 'private');
INSERT INTO message VALUES (288, NULL, 20, '2012-12-13 21:49:15.151', 'Bem, agora vou trabalhar', 1, NULL, '2012-12-13 21:49:15.151', 'public');
INSERT INTO message VALUES (245, '100000704093511_514338771932921', 18, '2012-12-13 02:46:39.30264', 'hello world! xD', NULL, NULL, '2012-12-13 02:46:39.30264', 'post');
INSERT INTO message VALUES (246, NULL, 18, '2012-12-13 02:48:25.114', 'oi oi', 2, NULL, '2012-12-13 02:48:25.114', 'public');
INSERT INTO message VALUES (247, NULL, 18, '2012-12-13 02:48:55.276', 'Oi Si!', 14, NULL, '2012-12-13 02:48:55.276', 'private');
INSERT INTO message VALUES (243, NULL, 1, '2012-12-12 18:20:21.384', 'u just mean...', 8, '2012-12-13 03:00:14.466406', '2012-12-12 18:20:21.384', 'private');
INSERT INTO message VALUES (248, NULL, 8, '2012-12-13 03:00:22.368', 'noooo im awesome', 1, '2012-12-13 03:21:43.264289', '2012-12-13 03:00:22.368', 'private');
INSERT INTO message VALUES (249, NULL, 1, '2012-12-13 03:21:46.496', 'ih ih', 8, NULL, '2012-12-13 03:21:46.496', 'private');
INSERT INTO message VALUES (250, NULL, 1, '2012-12-13 03:21:52.279', 'i just hate u...', 8, NULL, '2012-12-13 03:21:52.279', 'private');
INSERT INTO message VALUES (251, NULL, 1, '2012-12-13 03:21:55.326', 'ih ih ih', 8, NULL, '2012-12-13 03:21:55.326', 'private');
INSERT INTO message VALUES (253, '1237919046_4347022033896', 19, '2012-12-13 20:22:31.358298', 'Jagshemash', NULL, NULL, '2012-12-15 07:09:21.658955', 'post');
INSERT INTO message VALUES (252, NULL, 3, '2012-12-13 20:19:51.738', 'you are here!!!', 19, '2012-12-13 20:24:09.742988', '2012-12-13 20:19:51.738', 'private');
INSERT INTO message VALUES (254, NULL, 3, '2012-12-13 21:40:02.988', 'olá!', 20, '2012-12-13 21:42:54.232049', '2012-12-13 21:40:02.988', 'private');
INSERT INTO message VALUES (256, NULL, 20, '2012-12-13 21:43:21.231', 'Isto é o teu chat então', 3, '2012-12-13 21:43:38.554732', '2012-12-13 21:43:21.231', 'private');
INSERT INTO message VALUES (255, NULL, 20, '2012-12-13 21:43:07.828', 'Isso foi para mim? xD', 3, '2012-12-13 21:43:38.570398', '2012-12-13 21:43:07.828', 'private');
INSERT INTO message VALUES (257, NULL, 3, '2012-12-13 21:43:45.696', 'não isto são mensagens privatas', 20, '2012-12-13 21:43:47.899809', '2012-12-13 21:43:45.696', 'private');
INSERT INTO message VALUES (262, NULL, 20, '2012-12-13 21:44:04.348', 'O chat do facebook, queria eu dizer :P', 3, '2012-12-13 21:44:20.457557', '2012-12-13 21:44:04.348', 'private');
INSERT INTO message VALUES (260, NULL, 20, '2012-12-13 21:43:55.444', 'Ah ok', 3, '2012-12-13 21:44:20.917957', '2012-12-13 21:43:55.444', 'private');
INSERT INTO message VALUES (264, NULL, 20, '2012-12-13 21:45:01.147', 'Estou a explorar, tem lá calma', 3, '2012-12-13 21:45:06.18154', '2012-12-13 21:45:01.147', 'private');
INSERT INTO message VALUES (267, NULL, 3, '2012-12-13 21:45:18.003', 'eu estou lá', 20, '2012-12-13 21:45:19.239223', '2012-12-13 21:45:18.003', 'private');
INSERT INTO message VALUES (266, NULL, 3, '2012-12-13 21:45:10.323', 'ok ok...', 20, '2012-12-13 21:45:19.258902', '2012-12-13 21:45:10.323', 'private');
INSERT INTO message VALUES (263, NULL, 3, '2012-12-13 21:44:39.779', 'y u no here?', 20, '2012-12-13 21:45:19.275421', '2012-12-13 21:44:39.779', 'private');
INSERT INTO message VALUES (261, NULL, 3, '2012-12-13 21:44:00.281', 'vai a websockets', 20, '2012-12-13 21:45:19.294569', '2012-12-13 21:44:00.281', 'private');
INSERT INTO message VALUES (259, NULL, 3, '2012-12-13 21:43:55.359', 'e na tab de tecnologia', 20, '2012-12-13 21:45:19.312975', '2012-12-13 21:43:55.359', 'private');
INSERT INTO message VALUES (258, NULL, 3, '2012-12-13 21:43:50.809', 'chat vai a chatroom', 20, '2012-12-13 21:45:19.324956', '2012-12-13 21:43:50.809', 'private');
INSERT INTO message VALUES (269, NULL, 3, '2012-12-13 21:45:25.367', 'xD', 20, '2012-12-13 21:45:37.833027', '2012-12-13 21:45:25.367', 'private');
INSERT INTO message VALUES (268, NULL, 3, '2012-12-13 21:45:24.479', 'e só estamos nós os 2 online', 20, '2012-12-13 21:45:37.993685', '2012-12-13 21:45:24.479', 'private');
INSERT INTO message VALUES (274, NULL, 20, '2012-12-13 21:45:54.603', 'Que boa cena. Funciona?', 1, NULL, '2012-12-13 21:45:54.603', 'public');
INSERT INTO message VALUES (275, NULL, 3, '2012-12-13 21:45:59.377', 'que te parece?', 1, NULL, '2012-12-13 21:45:59.377', 'public');
INSERT INTO message VALUES (276, NULL, 3, '2012-12-13 21:46:04.351', 'websocket a bombar aqui!', 1, NULL, '2012-12-13 21:46:04.351', 'public');
INSERT INTO message VALUES (277, NULL, 3, '2012-12-13 21:46:05.894', 'js', 1, NULL, '2012-12-13 21:46:05.894', 'public');
INSERT INTO message VALUES (272, NULL, 20, '2012-12-13 21:45:34.725', 'Pois já reparei, que romântico', 3, '2012-12-13 21:46:35.592619', '2012-12-13 21:45:34.725', 'private');
INSERT INTO message VALUES (271, NULL, 20, '2012-12-13 21:45:29.038', 'xD', 3, '2012-12-13 21:46:35.806088', '2012-12-13 21:45:29.038', 'private');
INSERT INTO message VALUES (270, NULL, 20, '2012-12-13 21:45:28.13', 'Mas contigo já eu estou farto de falar ', 3, '2012-12-13 21:46:35.822342', '2012-12-13 21:45:28.13', 'private');
INSERT INTO message VALUES (265, NULL, 20, '2012-12-13 21:45:08.994', 'E não conheço ninguém por estes lados :P', 3, '2012-12-13 21:46:35.855618', '2012-12-13 21:45:08.994', 'private');
INSERT INTO message VALUES (273, NULL, 3, '2012-12-13 21:45:50.982', 'xD', 20, '2012-12-13 21:47:53.370883', '2012-12-13 21:45:50.982', 'private');
INSERT INTO message VALUES (278, NULL, 20, '2012-12-13 21:48:11.534', 'E porque é que desapareceu tudo? xD', 1, NULL, '2012-12-13 21:48:11.534', 'public');
INSERT INTO message VALUES (279, NULL, 3, '2012-12-13 21:48:12.046', 'só não vês é o histórico', 1, NULL, '2012-12-13 21:48:12.046', 'public');
INSERT INTO message VALUES (280, NULL, 20, '2012-12-13 21:48:21.196', 'Pois :P', 1, NULL, '2012-12-13 21:48:21.196', 'public');
INSERT INTO message VALUES (281, NULL, 3, '2012-12-13 21:48:22.08', 'pq isto é tudo enviado em realtime', 1, NULL, '2012-12-13 21:48:22.08', 'public');
INSERT INTO message VALUES (282, NULL, 20, '2012-12-13 21:48:27.642', 'E não há smileys', 1, NULL, '2012-12-13 21:48:27.642', 'public');
INSERT INTO message VALUES (283, NULL, 3, '2012-12-13 21:48:27.776', 'e submitted para a base de dados', 1, NULL, '2012-12-13 21:48:27.776', 'public');
INSERT INTO message VALUES (284, NULL, 3, '2012-12-13 21:48:32.972', 'qd estiver acabado', 1, NULL, '2012-12-13 21:48:32.972', 'public');
INSERT INTO message VALUES (285, NULL, 3, '2012-12-13 21:48:39.646', 'o autor pode fechar a bd', 1, NULL, '2012-12-13 21:48:39.646', 'public');
INSERT INTO message VALUES (286, NULL, 3, '2012-12-13 21:48:59.952', 'e ai sim dp podem consultar tudo', 1, NULL, '2012-12-13 21:48:59.952', 'public');
INSERT INTO message VALUES (287, NULL, 3, '2012-12-13 21:49:04.585', 'não...', 1, NULL, '2012-12-13 21:49:04.585', 'public');
INSERT INTO message VALUES (289, NULL, 20, '2012-12-13 21:49:19.156', 'Que a minha vida não é isto :P', 1, NULL, '2012-12-13 21:49:19.156', 'public');
INSERT INTO message VALUES (290, NULL, 2, '2012-12-14 02:35:00', 'please', 19, NULL, '2012-12-14 02:35:00', 'private');
INSERT INTO message VALUES (291, NULL, 3, '2012-12-14 02:33:35.711', 'olá!', 1, '2012-12-14 02:42:26.921703', '2012-12-14 02:33:35.711', 'private');
INSERT INTO message VALUES (292, NULL, 1, '2012-12-15 06:43:41.694', 'come online!!!!', 11, '2012-12-15 06:48:03.284552', '2012-12-15 06:43:41.694', 'private');
INSERT INTO message VALUES (299, NULL, 11, '2012-12-15 06:49:03.003', 'got it. ', 1, '2012-12-15 06:49:09.471159', '2012-12-15 06:49:03.003', 'private');
INSERT INTO message VALUES (293, NULL, 11, '2012-12-15 06:48:15.676', 'here. now what?', 1, '2012-12-15 06:49:09.513395', '2012-12-15 06:48:15.676', 'private');
INSERT INTO message VALUES (301, NULL, 1, '2012-12-15 06:55:12.189', 'http://9gag.com/gag/6065387', 11, '2012-12-15 06:56:21.962248', '2012-12-15 06:55:12.189', 'private');
INSERT INTO message VALUES (300, NULL, 1, '2012-12-15 06:53:30.679', 'thank u!!', 11, '2012-12-15 06:56:21.976887', '2012-12-15 06:53:30.679', 'private');
INSERT INTO message VALUES (298, NULL, 1, '2012-12-15 06:48:47.805', 'click it and the close', 11, '2012-12-15 06:56:21.999302', '2012-12-15 06:48:47.805', 'private');
INSERT INTO message VALUES (297, NULL, 1, '2012-12-15 06:48:42.142', 'there is a little button', 11, '2012-12-15 06:56:22.009882', '2012-12-15 06:48:42.142', 'private');
INSERT INTO message VALUES (296, NULL, 1, '2012-12-15 06:48:36.335', 'and on the right upper corner', 11, '2012-12-15 06:56:22.021552', '2012-12-15 06:48:36.335', 'private');
INSERT INTO message VALUES (295, NULL, 1, '2012-12-15 06:48:29.68', 'open the one you created', 11, '2012-12-15 06:56:22.032507', '2012-12-15 06:48:29.68', 'private');
INSERT INTO message VALUES (294, NULL, 1, '2012-12-15 06:48:21.98', 'go to chatrooms', 11, '2012-12-15 06:56:22.043395', '2012-12-15 06:48:21.98', 'private');
INSERT INTO message VALUES (309, NULL, 1, '2012-12-15 07:14:03.277', 'Um post!', 3, NULL, '2012-12-15 07:14:03.277', 'public');
INSERT INTO message VALUES (312, NULL, 1, '2012-12-15 17:09:30.296', 'ola!!', 22, '2012-12-15 17:09:36.868816', '2012-12-15 17:09:30.296', 'private');
INSERT INTO message VALUES (304, NULL, 5, '2012-12-15 07:10:19.434', 'gordo', 1, '2012-12-15 07:10:21.741858', '2012-12-15 07:10:19.434', 'private');
INSERT INTO message VALUES (305, NULL, 5, '2012-12-15 07:10:30.798', 'lol', 1, '2012-12-15 07:10:38.019197', '2012-12-15 07:10:30.798', 'private');
INSERT INTO message VALUES (306, NULL, 1, '2012-12-15 07:10:30.906', 'cenas', 5, '2012-12-15 07:12:19.774939', '2012-12-15 07:10:30.906', 'private');
INSERT INTO message VALUES (307, NULL, 1, '2012-12-15 07:13:00', 'Pufabor!', 5, '2012-12-15 07:13:08.492605', '2012-12-15 07:13:00', 'private');
INSERT INTO message VALUES (313, NULL, 1, '2012-12-15 17:09:43.925969', 'post!', NULL, NULL, '2012-12-15 17:09:43.925969', 'post');
INSERT INTO message VALUES (310, NULL, 1, '2012-12-15 16:50:00.892', 'relató;/assets/attaches/report_719973068173730992.pdf;', 3, NULL, '2012-12-15 16:50:00.892', 'public');
INSERT INTO message VALUES (311, NULL, 1, '2012-12-15 16:50:01.531', 'rio;/assets/attaches/report_1800207152420294641.pdf;', 3, NULL, '2012-12-15 16:50:01.531', 'public');
INSERT INTO message VALUES (302, NULL, 1, '2012-12-15 07:09:29.063221', 'ol�', NULL, NULL, '2012-12-15 17:09:24.497053', 'post');
INSERT INTO message VALUES (314, NULL, 1, '2012-12-15 23:56:11.502', 'hello!;', 4, NULL, '2012-12-15 23:56:11.502', 'public');


--
-- TOC entry 1979 (class 0 OID 42360)
-- Dependencies: 171 1981
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO users VALUES (22, 'João Rafael', '1143884623@facebook.com', '1143884623', 'Coimbra', 'Portugal', '2012-12-15', 'm', '', true, true, NULL);
INSERT INTO users VALUES (4, 'John Amdhdjgdcfhd Fallerstein', '100004840743684@facebook.com', '100004840743684', 'Unknown', 'Unknown', '2012-12-11', 'm', '', true, true, NULL);
INSERT INTO users VALUES (6, 'Ricardo Rogério Fernandes', '1275794240@facebook.com', '1275794240', 'Coimbra', ' Portugal', '2012-12-12', 'm', '', true, true, NULL);
INSERT INTO users VALUES (7, 'Dick Amdhijaebdfh Carrierosky', '100004890152468@facebook.com', '100004890152468', 'Unknown', 'Unknown', '2012-12-12', 'm', '', true, true, NULL);
INSERT INTO users VALUES (8, 'Adriana Ferrugento', 'adrianaferrugento@gmail.com', NULL, 'Coimbra', 'Portugal', '1992-07-30', 'f', 'e10adc3949ba59abbe56e057f20f883e', true, true, NULL);
INSERT INTO users VALUES (9, 'Mariana', 'marrlourenco@gmail.com', NULL, '', '', '1992-11-25', 'f', '9aa6e5f2256c17d2d430b100032b997c', true, true, NULL);
INSERT INTO users VALUES (10, 'TeuMacho', 'voutepegar@gmail.com', NULL, '', '', '2012-12-12', 'm', '79a316ec4f9d33da74545aa303b07f57', true, true, NULL);
INSERT INTO users VALUES (12, 'Adriana Ferrugento', '100000987097357@facebook.com', '100000987097357', 'Unknown', 'Unknown', '2012-12-12', 'f', '', true, true, NULL);
INSERT INTO users VALUES (13, 'Daniel Vaz', 'djrv335577@yahoo.com.br', NULL, 'Coimbra', 'Portugal', '1990-07-11', 'm', 'e10adc3949ba59abbe56e057f20f883e', true, true, NULL);
INSERT INTO users VALUES (14, 'Sílvia Jesus', '100000400120398@facebook.com', '100000400120398', 'Coimbra', ' Portugal', '2012-12-12', 'f', '', true, true, NULL);
INSERT INTO users VALUES (15, 'Daniel Vaz', '100000775093499@facebook.com', '100000775093499', 'Coimbra', ' Portugal', '2012-12-12', 'm', '', true, true, NULL);
INSERT INTO users VALUES (2, 'Alexandre Santos', 'alexcsantos1991@gmail.com', NULL, 'Coimbra', 'Portugal', '1991-10-14', 'm', 'e10adc3949ba59abbe56e057f20f883e', true, true, NULL);
INSERT INTO users VALUES (17, 'Patricia Amdhdbfjdkd Sidhuberg', '100004842604004@facebook.com', '100004842604004', 'Unknown', 'Unknown', '2012-12-12', 'f', '', true, true, NULL);
INSERT INTO users VALUES (18, 'Milton Seco', '100000704093511@facebook.com', '100000704093511', 'Coimbra', 'Portugal', '2012-12-13', 'm', '', true, true, NULL);
INSERT INTO users VALUES (1, 'João Ferreira', 'jooaooferreira@gmail.com', NULL, 'Coimbra', 'Portugal', '1991-03-18', 'm', 'e10adc3949ba59abbe56e057f20f883e', true, true, NULL);
INSERT INTO users VALUES (23, 'Andreia Gonçalves', '100002160072560@facebook.com', '100002160072560', 'Unknown', 'Unknown', '2012-12-16', 'f', '', true, true, NULL);
INSERT INTO users VALUES (3, 'João Ferreira', '1606740705@facebook.com', '1606740705', 'Coimbra', ' Portugal', '2012-12-11', 'm', '', true, true, NULL);
INSERT INTO users VALUES (19, 'Joao Machado', '1237919046@facebook.com', '1237919046', 'Unknown', 'Unknown', '2012-12-13', 'm', '', true, true, NULL);
INSERT INTO users VALUES (20, 'Ivo Correia', 'icorreia@student.dei.uc.pt', NULL, 'Coimbra', 'Portugal', '1990-05-02', 'm', '493b8d7e0cd471b7a74322c50fc4b791', true, true, NULL);
INSERT INTO users VALUES (11, 'Leah Pope', '583107809@facebook.com', '583107809', 'Coimbra', ' Portugal', '2012-12-12', 'f', '', true, true, NULL);
INSERT INTO users VALUES (21, 'Joaquim Soares', 'jocas@mail.com', NULL, 'Juarez', 'Mexico', '1993-06-15', 'm', 'e10adc3949ba59abbe56e057f20f883e', false, true, NULL);
INSERT INTO users VALUES (5, 'Alexandre Santos', '1132836787@facebook.com', '1132836787', 'Coimbra', ' Portugal', '2012-12-11', 'm', '', true, true, NULL);


--
-- TOC entry 1980 (class 0 OID 42372)
-- Dependencies: 172 1981
-- Data for Name: vote; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO vote VALUES (2, 2, 3);
INSERT INTO vote VALUES (1, 2, 2);
INSERT INTO vote VALUES (1, 1, 3);
INSERT INTO vote VALUES (2, 1, 1);
INSERT INTO vote VALUES (2, 11, 3);
INSERT INTO vote VALUES (1, 5, 1);
INSERT INTO vote VALUES (3, 1, 2);
INSERT INTO vote VALUES (4, 1, 3);


--
-- TOC entry 1957 (class 2606 OID 42370)
-- Dependencies: 171 171 1982
-- Name: id_email; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT id_email UNIQUE (email);


--
-- TOC entry 1939 (class 2606 OID 42304)
-- Dependencies: 162 162 1982
-- Name: pk_attach; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY attach
    ADD CONSTRAINT pk_attach PRIMARY KEY (attachid);


--
-- TOC entry 1946 (class 2606 OID 42330)
-- Dependencies: 166 166 1982
-- Name: pk_chatroom; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY chatroom
    ADD CONSTRAINT pk_chatroom PRIMARY KEY (chatid);


--
-- TOC entry 1941 (class 2606 OID 42316)
-- Dependencies: 164 164 1982
-- Name: pk_comment; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY comment
    ADD CONSTRAINT pk_comment PRIMARY KEY (commentid);


--
-- TOC entry 1951 (class 2606 OID 42341)
-- Dependencies: 167 167 167 1982
-- Name: pk_connection; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY connection
    ADD CONSTRAINT pk_connection PRIMARY KEY (chatid, userid);


--
-- TOC entry 1954 (class 2606 OID 42356)
-- Dependencies: 169 169 1982
-- Name: pk_message; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY message
    ADD CONSTRAINT pk_message PRIMARY KEY (messageid);


--
-- TOC entry 1959 (class 2606 OID 42368)
-- Dependencies: 171 171 1982
-- Name: pk_user; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT pk_user PRIMARY KEY (userid);


--
-- TOC entry 1963 (class 2606 OID 42377)
-- Dependencies: 172 172 172 1982
-- Name: pk_vote; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY vote
    ADD CONSTRAINT pk_vote PRIMARY KEY (chatid, userid);


--
-- TOC entry 1936 (class 1259 OID 42305)
-- Dependencies: 162 1982
-- Name: attach_pk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX attach_pk ON attach USING btree (attachid);


--
-- TOC entry 1943 (class 1259 OID 42331)
-- Dependencies: 166 1982
-- Name: chatroom_pk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX chatroom_pk ON chatroom USING btree (chatid);


--
-- TOC entry 1947 (class 1259 OID 42342)
-- Dependencies: 167 167 1982
-- Name: connection_pk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX connection_pk ON connection USING btree (chatid, userid);


--
-- TOC entry 1937 (class 1259 OID 42318)
-- Dependencies: 162 1982
-- Name: contains_fk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX contains_fk ON attach USING btree (messageid);


--
-- TOC entry 1955 (class 1259 OID 50634)
-- Dependencies: 171 1982
-- Name: email; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX email ON users USING btree (email);


--
-- TOC entry 1961 (class 1259 OID 42380)
-- Dependencies: 172 1982
-- Name: gives_fk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX gives_fk ON vote USING btree (userid);


--
-- TOC entry 1948 (class 1259 OID 42344)
-- Dependencies: 167 1982
-- Name: has_fk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX has_fk ON connection USING btree (userid);


--
-- TOC entry 1949 (class 1259 OID 42343)
-- Dependencies: 167 1982
-- Name: links_fk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX links_fk ON connection USING btree (chatid);


--
-- TOC entry 1952 (class 1259 OID 42357)
-- Dependencies: 169 1982
-- Name: message_pk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX message_pk ON message USING btree (messageid);


--
-- TOC entry 1944 (class 1259 OID 42332)
-- Dependencies: 166 1982
-- Name: owns_fk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX owns_fk ON chatroom USING btree (ownerid);


--
-- TOC entry 1942 (class 1259 OID 42317)
-- Dependencies: 164 1982
-- Name: postcomment_pk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX postcomment_pk ON comment USING btree (postid);


--
-- TOC entry 1964 (class 1259 OID 42379)
-- Dependencies: 172 1982
-- Name: to_fk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX to_fk ON vote USING btree (chatid);


--
-- TOC entry 1960 (class 1259 OID 42371)
-- Dependencies: 171 1982
-- Name: user_pk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX user_pk ON users USING btree (userid);


--
-- TOC entry 1965 (class 1259 OID 42378)
-- Dependencies: 172 172 1982
-- Name: vote_pk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX vote_pk ON vote USING btree (chatid, userid);


--
-- TOC entry 1973 (class 2620 OID 50633)
-- Dependencies: 185 172 1982
-- Name: updaterating; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER updaterating AFTER INSERT OR UPDATE ON vote FOR EACH ROW EXECUTE PROCEDURE calcrating();


--
-- TOC entry 1966 (class 2606 OID 42381)
-- Dependencies: 169 162 1953 1982
-- Name: fk_attach_contains_message; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY attach
    ADD CONSTRAINT fk_attach_contains_message FOREIGN KEY (messageid) REFERENCES message(messageid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- TOC entry 1967 (class 2606 OID 42386)
-- Dependencies: 171 1958 166 1982
-- Name: fk_chatroom_owns_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY chatroom
    ADD CONSTRAINT fk_chatroom_owns_user FOREIGN KEY (ownerid) REFERENCES users(userid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- TOC entry 1968 (class 2606 OID 42391)
-- Dependencies: 167 1958 171 1982
-- Name: fk_connecti_has_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY connection
    ADD CONSTRAINT fk_connecti_has_user FOREIGN KEY (userid) REFERENCES users(userid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- TOC entry 1969 (class 2606 OID 42396)
-- Dependencies: 166 167 1945 1982
-- Name: fk_connecti_links_chatroom; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY connection
    ADD CONSTRAINT fk_connecti_links_chatroom FOREIGN KEY (chatid) REFERENCES chatroom(chatid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- TOC entry 1970 (class 2606 OID 42401)
-- Dependencies: 171 1958 169 1982
-- Name: fk_message_sends_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY message
    ADD CONSTRAINT fk_message_sends_user FOREIGN KEY (senderid) REFERENCES users(userid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- TOC entry 1971 (class 2606 OID 42406)
-- Dependencies: 1958 172 171 1982
-- Name: fk_vote_gives_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY vote
    ADD CONSTRAINT fk_vote_gives_user FOREIGN KEY (userid) REFERENCES users(userid) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- TOC entry 1972 (class 2606 OID 42411)
-- Dependencies: 1945 172 166 1982
-- Name: fk_vote_to_chatroom; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY vote
    ADD CONSTRAINT fk_vote_to_chatroom FOREIGN KEY (chatid) REFERENCES chatroom(chatid) ON UPDATE RESTRICT ON DELETE RESTRICT;


-- Completed on 2012-12-16 03:27:32 WET

--
-- PostgreSQL database dump complete
--

