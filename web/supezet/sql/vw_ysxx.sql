CREATE view vw_ysxx as
select id,ysmxm,jdys,ztys,ysyys,jdys-ysyys-ztys as ksyys,mainid from 
(select id,mainid,ysmxm,jdys, 
(select  isnull(sum(bcsyje),0)from uf_htfplcjl where ysxx = uf_xmxxb_dt1.id and mxzt = 0) as ztys,
(select isnull(sum(bcsyje),0)  from uf_htfplcjl where ysxx = uf_xmxxb_dt1.id and mxzt = 1) as ysyys
from uf_xmxxb_dt1) a

alter view vw_ysxx as
(select id,ysmxm,jdys,ztys,ysyys,jdys-ysyys-ztys as ksyys,mainid from 
(select id,mainid,ysmxm,jdys, 
(select isnull(sum(bcsyje),0) from uf_htfplcjl where ysxx = uf_xmxxb_dt1.id and sfyc = 0 and (select currentnodetype from workflow_requestbase where requestid = uf_htfplcjl.fplc) != 3) as ztys,
((select isnull(sum(bcsyje),0) from uf_htfplcjl where ysxx = uf_xmxxb_dt1.id and sfyc = 0 and (sftz = 0 or (select currentnodetype from workflow_requestbase where requestid = uf_htfplcjl.fplc) = 3)) + (case)) as ysyys
from uf_xmxxb_dt1) a)



create view vw_ysxx_bb as (
    select vw_ysxx_cjzzf.*,isnull(vw_cb.cbje,0) as zcbje from vw_ysxx_cjzzf left join vw_cb on vw_ysxx_cjzzf.id = vw_cb.ys
)

CREATE view vw_ysxx_sq as
select id,ysxxmc,jdys,ztys,ysyys,jdys-ysyys-ztys as ksyys,mainid from 
(select id,mainid,ysxxmc,jdys, 
(0) as ztys,
(select isnull(sum(bcsyje),0) from uf_sqyssysj where ysxx = uf_xmxxb_dt2.id and sfyc = 0) as ysyys
from uf_xmxxb_dt2) a

-- 成本统计视图
CREATE view vw_cb as
select ht.ys,(
    sum(isnull(cb.JE1,0)*ratio)
) as cbje from (
        select dt.ysmx as ys, dt.fyjeybz/main.zcgjebb as ratio, main.dyhtbh as htbh from uf_jefpbd main inner join uf_jefpbd_dt1 dt on main.id=dt.mainid where  dt.ysmx is not null and main.sxzt!=1
    ) as ht left join ERP_rk as cb on ht.htbh = cb.PMDT001 group by ht.ys ; 

-- 车间制造费分摊视图
select * from ERP_cjzzf erp left join uf_xmxxb oa on erp.SFAA028 = oa.erpxm and erp.XCBKLD = oa.yszt;

create view vw_ysbz_cjzzf as (
select erp.JE,oa.id from ERP_cjzzf as erp right join uf_xmxxb oa on erp.SFAA028 = oa.erpxm and erp.XCBKLD = oa.yszt
)

select id,ysmxm,jdys,ztys,ysyys, case when ysmxm like '车间制造费' then (select isnull(erp.JE,0) from vw_ysbz_cjzzf as erp where erp.id = mainid)  else ksyys end,mainid from vw_ysxx

create view vw_ysxx_cjzzf as (
select vw_ysxx.id,ysmxm,jdys,ztys, case when ysmxm like '车间制造费' then isnull(JE,0) else ysyys end as ysyys,ksyys,mainid from vw_ysxx left join vw_ysbz_cjzzf on vw_ysxx.mainid = vw_ysbz_cjzzf.id)



select * from 
    (select t1.[id] as t1_id,t1.[requestId] as t1_requestId,t1.[formmodeid] as t1_formmodeid,t1.[modedatacreater] as t1_modedatacreater,t1.[modedatacreatertype] as t1_modedatacreatertype,t1.[modedatacreatedate] as t1_modedatacreatedate,t1.[modedatacreatetime] as t1_modedatacreatetime,t1.[MODEUUID] as t1_MODEUUID,t1.[xmysmxxx] as t1_xmysmxxx,t1.[xmmc] as t1_xmmc,t1.[sxzt] as t1_sxzt,t1.[xmzt] as t1_xmzt,t1.[xmjl] as t1_xmjl,t1.[sx] as t1_sx,t1.[htje] as t1_htje,t1.[sqysje] as t1_sqysje,t1.[yszt] as t1_yszt,t1.[erpxm] as t1_erpxm,t1.[ysztjl] as t1_ysztjl,t1.[ejxmfenl] as t1_ejxmfenl,t1.[zysje] as t1_zysje,t1.[mll] as t1_mll,t1.[sqzzxrq] as t1_sqzzxrq,t1.[modedatamodifier] as t1_modedatamodifier,t1.[modedatamodifydatetime] as t1_modedatamodifydatetime,t1.[xmbhwb] as t1_xmbhwb,t1.[swjl] as t1_swjl,t1.[xmfl] as t1_xmfl,t1.[xmzzysgsfw] as t1_xmzzysgsfw,t1.[zzxmllbbxs] as t1_zzxmllbbxs,t1.[ysmll] as t1_ysmll,t1.[hjsyys] as t1_hjsyys,t2.[id] as t2_id,t2.[ysmxm] as t2_ysmxm,t2.[jdys] as t2_jdys,t2.[ztys] as t2_ztys,t2.[ysyys] as t2_ysyys,t2.[ksyys] as t2_ksyys,t2.[mainid] as t2_mainid,t2.[zcbje] as t2_zcbje,t3.[id] as t3_id,t3.[ysxxmc] as t3_ysxxmc,t3.[jdys] as t3_jdys,t3.[ztys] as t3_ztys,t3.[ysyys] as t3_ysyys,t3.[ksyys] as t3_ksyys,t3.[mainid] as t3_mainid,t4.[id] as t4_id,t4.[ismain] as t4_ismain )
t where 1=1 order by t4_ismain desc,t1_modedatacreatedate desc


 select * from uf_xmxxb  t1 LEFT join vw_ysxx_bb  t2  on t1.id = t2.mainid LEFT join vw_ysxx_sq  t3  on t1.id = t3.mainid LEFT join vw_xmxxb_ismain  t4  on t1.id = t4.id


 SELECT     dbo.vw_ysxx.id, dbo.vw_ysxx.ysmxm, dbo.vw_ysxx.jdys, dbo.vw_ysxx.ztys, CASE WHEN ysmxm LIKE '车间制造费' THEN isnull(JE, 0) + ysyys when ysmxm like '管理费' then (select sum(je) from dbo.vs_zxglsy where dbo.vs_zxglsy.id = dbo.vw_ysxx.id) ELSE ysyys END AS ysyys, 
                      dbo.vw_ysxx.ksyys, dbo.vw_ysxx.mainid
FROM         dbo.vw_ysxx LEFT OUTER JOIN
                      dbo.vw_ysbz_cjzzf ON dbo.vw_ysxx.mainid = dbo.vw_ysbz_cjzzf.id


alter vw_ysbz_cjzzf as (
                      SELECT     erp.JE, oa.id
FROM         dbo.uf_cjzzf AS erp RIGHT OUTER JOIN
                      dbo.uf_xmxxb AS oa ON erp.SFAA028 = oa.erpxm AND erp.XCBKLD = oa.yszt )

create view vw_ysbz_glf as 



alter view vw_usbl as(
select htje*1.00/zys as precent,id,m.erpxm,m.yszt,sqzzxrq from uf_xmxxb m left join (select erpxm,yszt,SUM(htje) as zys from uf_xmxxb group by erpxm,yszt) as sums  on m.erpxm = sums.erpxm and m.yszt = sums.yszt
)

alter view vs_sqsy as (
select sqbl.id,je*precent as je from (select uf_xmxxb_dt2.*,precent,yszt,erpxm,sqzzxrq from uf_xmxxb_dt2 left join vw_usbl divide on divide.id = uf_xmxxb_dt2.mainid ) as sqbl inner join uf_bxzxje on sqbl.yszt = uf_bxzxje.gs and sqbl.erpxm = uf_bxzxje.xmh where DateDiff(day,uf_bxzxje.rq,isnull(sqbl.sqzzxrq,'2099-12-31')) > 0
)

alter view vs_zxglsy as(
select sqbl.id,je*precent as je from 
	(select uf_xmxxb_dt1.*,precent,yszt,erpxm,sqzzxrq from uf_xmxxb_dt1 left join vw_usbl divide on divide.id = uf_xmxxb_dt1.mainid where ysmxm like '管理费') as sqbl
inner join uf_bxzxje on sqbl.yszt = uf_bxzxje.gs and sqbl.erpxm = uf_bxzxje.xmh where DateDiff(day,uf_bxzxje.rq,isnull(sqbl.sqzzxrq,'2099-12-31')) <= 0)

alter view vw_ysxx_cjzzf as (
 SELECT     dbo.vw_ysxx.id, dbo.vw_ysxx.ysmxm, dbo.vw_ysxx.jdys, dbo.vw_ysxx.ztys, CASE WHEN ysmxm LIKE '车间制造费' THEN isnull(JE, 0) when ysmxm like '管理费' then (select isnull(sum(je),0) from dbo.vs_zxglsy where dbo.vs_zxglsy.id = dbo.vw_ysxx.id) ELSE ysyys END AS ysyys, 
                      dbo.vw_ysxx.ksyys, dbo.vw_ysxx.mainid
FROM         dbo.vw_ysxx LEFT OUTER JOIN
                      dbo.vw_ysbz_cjzzf ON dbo.vw_ysxx.mainid = dbo.vw_ysbz_cjzzf.id)

alter view vw_cb as (                   
SELECT     ht.ys, SUM(ISNULL(cb.JE1, 0) * ht.ratio) AS cbje
FROM         (SELECT     dt.ysmx AS ys, dt.fyjeybz / main.zcgjebb AS ratio, main.dyhtbh AS htbh
                       FROM          dbo.uf_jefpbd AS main INNER JOIN
                                              dbo.uf_jefpbd_dt1 AS dt ON main.id = dt.mainid
                       WHERE      (dt.ysmx IS NOT NULL) AND (main.sxzt <> 1)) AS ht LEFT OUTER JOIN
                      dbo.uf_cb AS cb ON ht.htbh = cb.PMDT001
GROUP BY ht.ys
)

alter view vw_ysxx_cjzzf as (
	SELECT     dbo.vw_ysxx.id, dbo.vw_ysxx.ysmxm, dbo.vw_ysxx.jdys, dbo.vw_ysxx.ztys, CASE WHEN ysmxm LIKE '车间制造费' THEN isnull(JE, 0) + ysyys
                      WHEN ysmxm LIKE '管理费' THEN
                          (SELECT     isnull(SUM(je), 0)
                            FROM          dbo.vs_zxglsy
                            WHERE      dbo.vs_zxglsy.id = dbo.vw_ysxx.id) ELSE ysyys END AS ysyys, dbo.vw_ysxx.ksyys, dbo.vw_ysxx.mainid
FROM         dbo.vw_ysxx LEFT OUTER JOIN
                      dbo.vw_ysbz_cjzzf ON dbo.vw_ysxx.mainid = dbo.vw_ysbz_cjzzf.id
)

create view  vw_ysxx_without_manage as (
                      select mainid, SUM(jdys) as jdys,SUM(ztys) as ztys,SUM(ysyys) as ysyys,SUM(jdys-ztys-ysyys) as syys from vw_ysxx_cjzzf where ysmxm not like '管理费' group by mainid)

                      alter view vw_ysbz_cjzzf as(
                      SELECT     isnull(erp.JE,0) * oa.precent as je, oa.id
FROM         dbo.uf_cjzzf AS erp RIGHT OUTER JOIN
                      dbo.vw_usbl AS oa ON erp.SFAA028 = oa.erpxm AND erp.XCBKLD = oa.yszt)

                      create view vw_zxmll as 
(select id ,(htje-ysje)*100.00/uf_xmxxb.htje zxmll  from uf_xmxxb left join (select mainid,SUM(case when (ysyys+ztys) > jdys then ysyys+ztys else jdys end) as ysje from vw_ysxx_bb where ysmxm not like '管理费' group by mainid) as zxtj on zxtj.mainid = uf_xmxxb.id)