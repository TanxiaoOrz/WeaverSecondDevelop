-- 预填写汇率
SELECT if ('$20222$'='$20217$',1.00,( select ooan005 from  ERP_HL  where bwb = '$20217$' and ooan002 = '$20222$' )) 

select if ('$20217$' = 'CNY', 1.00 , ( select 1/ooan005 from ERP_HL where bwb = '$20217$' and ooan002 = 'CNY'))


select ysxx,
