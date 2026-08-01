-- Local-development catalogue only. Executed by LocalCatalogueDataInitializer.
-- Prices, availability and restriction states are synthetic test data, not medical or pricing guidance.

insert into manufacturer (id, code, name_ar, name_en, normalized_name, active, created_at, updated_at, version) values
('30000000-0000-0000-0000-000000000001','GSK','جي إس كيه','GSK','جي اس كيه gsk',true,now(),now(),0),
('30000000-0000-0000-0000-000000000002','ABBOTT','أبوت','Abbott','ابوت abbott',true,now(),now(),0),
('30000000-0000-0000-0000-000000000003','NOVARTIS','نوفارتس','Novartis','نوفارتس novartis',true,now(),now(),0),
('30000000-0000-0000-0000-000000000004','SANOFI','سانوفي','Sanofi','سانوفي sanofi',true,now(),now(),0),
('30000000-0000-0000-0000-000000000005','AMOUN','آمون','Amoun','امون amoun',true,now(),now(),0),
('30000000-0000-0000-0000-000000000006','BOEHRINGER','بوهرنجر إنجلهايم','Boehringer Ingelheim','بوهرنجر انجلهايم boehringer ingelheim',true,now(),now(),0),
('30000000-0000-0000-0000-000000000007','HALEON','هاليون','Haleon','هاليون haleon',true,now(),now(),0),
('30000000-0000-0000-0000-000000000008','MERCK','ميرك','Merck','ميرك merck',true,now(),now(),0),
('30000000-0000-0000-0000-000000000009','NOVO_NORDISK','نوفو نورديسك','Novo Nordisk','نوفو نورديسك novo nordisk',true,now(),now(),0)
on conflict (code) do update set name_ar=excluded.name_ar, name_en=excluded.name_en,
 normalized_name=excluded.normalized_name, active=true, updated_at=now();

insert into active_ingredient (id, code, name_ar, name_en, normalized_name, active, created_at, updated_at, version) values
('31000000-0000-0000-0000-000000000001','PARACETAMOL','باراسيتامول','Paracetamol','باراسيتامول paracetamol',true,now(),now(),0),
('31000000-0000-0000-0000-000000000002','CAFFEINE','كافيين','Caffeine','كافيين caffeine',true,now(),now(),0),
('31000000-0000-0000-0000-000000000003','IBUPROFEN','إيبوبروفين','Ibuprofen','ايبوبروفين ibuprofen',true,now(),now(),0),
('31000000-0000-0000-0000-000000000004','AMOXICILLIN','أموكسيسيلين','Amoxicillin','اموكسيسيلين amoxicillin',true,now(),now(),0),
('31000000-0000-0000-0000-000000000005','CLAVULANIC_ACID','حمض كلافولانيك','Clavulanic acid','حمض كلافولانيك clavulanic acid',true,now(),now(),0),
('31000000-0000-0000-0000-000000000006','SALBUTAMOL','سالبوتامول','Salbutamol','سالبوتامول salbutamol',true,now(),now(),0),
('31000000-0000-0000-0000-000000000007','DICLOFENAC','ديكلوفيناك','Diclofenac','ديكلوفيناك diclofenac',true,now(),now(),0),
('31000000-0000-0000-0000-000000000008','INSULIN_GLARGINE','إنسولين جلارجين','Insulin glargine','انسولين جلارجين insulin glargine',true,now(),now(),0),
('31000000-0000-0000-0000-000000000009','BISOPROLOL','بيسوبرولول','Bisoprolol','بيسوبرولول bisoprolol',true,now(),now(),0),
('31000000-0000-0000-0000-000000000010','XYLOMETAZOLINE','زيلوميتازولين','Xylometazoline','زيلوميتازولين xylometazoline',true,now(),now(),0),
('31000000-0000-0000-0000-000000000011','TRAMADOL','ترامادول','Tramadol','ترامادول tramadol',true,now(),now(),0),
('31000000-0000-0000-0000-000000000012','RANITIDINE','رانيتيدين','Ranitidine','رانيتيدين ranitidine',true,now(),now(),0),
('31000000-0000-0000-0000-000000000013','SEMAGLUTIDE','سيماجلوتايد','Semaglutide','سيماجلوتايد semaglutide',true,now(),now(),0)
on conflict (code) do update set name_ar=excluded.name_ar, name_en=excluded.name_en,
 normalized_name=excluded.normalized_name, active=true, updated_at=now();

insert into dosage_form (id, code, name_ar, name_en, active, created_at, updated_at, version) values
('32000000-0000-0000-0000-000000000001','TABLET','أقراص','Tablets',true,now(),now(),0),
('32000000-0000-0000-0000-000000000002','CAPSULE','كبسولات','Capsules',true,now(),now(),0),
('32000000-0000-0000-0000-000000000003','INHALER','بخاخ استنشاق','Inhaler',true,now(),now(),0),
('32000000-0000-0000-0000-000000000004','INJECTION','حقن','Injection',true,now(),now(),0),
('32000000-0000-0000-0000-000000000005','NASAL_DROPS','نقط للأنف','Nasal drops',true,now(),now(),0),
('32000000-0000-0000-0000-000000000006','PREFILLED_PEN','قلم معبأ مسبقاً','Prefilled pen',true,now(),now(),0)
on conflict (code) do update set name_ar=excluded.name_ar, name_en=excluded.name_en,
 active=true, updated_at=now();

insert into medicine (id, name_ar, name_en, normalized_name_ar, normalized_name_en, manufacturer_id,
 prescription_required, restricted, restriction_code, storage_type, active, created_at, updated_at, version) values
('33000000-0000-0000-0000-000000000001','بانادول','Panadol','بانادول','panadol',(select id from manufacturer where code='HALEON'),false,false,null,'ROOM_TEMPERATURE',true,now(),now(),0),
('33000000-0000-0000-0000-000000000002','بانادول إكسترا','Panadol Extra','بانادول اكسترا','panadol extra',(select id from manufacturer where code='HALEON'),false,false,null,'ROOM_TEMPERATURE',true,now(),now(),0),
('33000000-0000-0000-0000-000000000003','بروفين','Brufen','بروفين','brufen',(select id from manufacturer where code='ABBOTT'),false,false,null,'ROOM_TEMPERATURE',true,now(),now(),0),
('33000000-0000-0000-0000-000000000004','أوجمنتين','Augmentin','اوجمنتين','augmentin',(select id from manufacturer where code='GSK'),true,false,null,'ROOM_TEMPERATURE',true,now(),now(),0),
('33000000-0000-0000-0000-000000000005','فنتولين','Ventolin','فنتولين','ventolin',(select id from manufacturer where code='GSK'),true,false,null,'ROOM_TEMPERATURE',true,now(),now(),0),
('33000000-0000-0000-0000-000000000006','كاتافلام','Cataflam','كاتافلام','cataflam',(select id from manufacturer where code='NOVARTIS'),true,false,null,'ROOM_TEMPERATURE',true,now(),now(),0),
('33000000-0000-0000-0000-000000000007','لانتوس','Lantus','لانتوس','lantus',(select id from manufacturer where code='SANOFI'),true,false,null,'REFRIGERATED',true,now(),now(),0),
('33000000-0000-0000-0000-000000000008','كونكور','Concor','كونكور','concor',(select id from manufacturer where code='MERCK'),true,false,null,'ROOM_TEMPERATURE',true,now(),now(),0),
('33000000-0000-0000-0000-000000000009','أوتريفين','Otrivin','اوتريفين','otrivin',(select id from manufacturer where code='NOVARTIS'),false,false,null,'ROOM_TEMPERATURE',true,now(),now(),0),
('33000000-0000-0000-0000-000000000010','ترامادول','Tramadol','ترامادول','tramadol',(select id from manufacturer where code='AMOUN'),true,true,'CONTROLLED_PRODUCT','ROOM_TEMPERATURE',true,now(),now(),0),
('33000000-0000-0000-0000-000000000011','رانيتيدين','Ranitidine','رانيتيدين','ranitidine',(select id from manufacturer where code='GSK'),true,false,null,'ROOM_TEMPERATURE',true,now(),now(),0),
('33000000-0000-0000-0000-000000000012','أوزمبيك','Ozempic','اوزمبيك','ozempic',(select id from manufacturer where code='NOVO_NORDISK'),true,false,null,'REFRIGERATED',true,now(),now(),0)
on conflict (id) do update set name_ar=excluded.name_ar, name_en=excluded.name_en,
 normalized_name_ar=excluded.normalized_name_ar, normalized_name_en=excluded.normalized_name_en,
 manufacturer_id=excluded.manufacturer_id, prescription_required=excluded.prescription_required,
 restricted=excluded.restricted, restriction_code=excluded.restriction_code, storage_type=excluded.storage_type,
 active=excluded.active, updated_at=now();

insert into medicine_active_ingredient (medicine_id, active_ingredient_id, sequence_number) values
('33000000-0000-0000-0000-000000000001',(select id from active_ingredient where code='PARACETAMOL'),1),
('33000000-0000-0000-0000-000000000002',(select id from active_ingredient where code='PARACETAMOL'),1),
('33000000-0000-0000-0000-000000000002',(select id from active_ingredient where code='CAFFEINE'),2),
('33000000-0000-0000-0000-000000000003',(select id from active_ingredient where code='IBUPROFEN'),1),
('33000000-0000-0000-0000-000000000004',(select id from active_ingredient where code='AMOXICILLIN'),1),
('33000000-0000-0000-0000-000000000004',(select id from active_ingredient where code='CLAVULANIC_ACID'),2),
('33000000-0000-0000-0000-000000000005',(select id from active_ingredient where code='SALBUTAMOL'),1),
('33000000-0000-0000-0000-000000000006',(select id from active_ingredient where code='DICLOFENAC'),1),
('33000000-0000-0000-0000-000000000007',(select id from active_ingredient where code='INSULIN_GLARGINE'),1),
('33000000-0000-0000-0000-000000000008',(select id from active_ingredient where code='BISOPROLOL'),1),
('33000000-0000-0000-0000-000000000009',(select id from active_ingredient where code='XYLOMETAZOLINE'),1),
('33000000-0000-0000-0000-000000000010',(select id from active_ingredient where code='TRAMADOL'),1),
('33000000-0000-0000-0000-000000000011',(select id from active_ingredient where code='RANITIDINE'),1),
('33000000-0000-0000-0000-000000000012',(select id from active_ingredient where code='SEMAGLUTIDE'),1)
on conflict (medicine_id, active_ingredient_id) do update set sequence_number=excluded.sequence_number;

insert into medicine_alias (id, medicine_id, alias, normalized_alias, created_at) values
('35000000-0000-0000-0000-000000000001','33000000-0000-0000-0000-000000000001','بنادول','بنادول',now()),
('35000000-0000-0000-0000-000000000002','33000000-0000-0000-0000-000000000001','Paracetamol tablets','paracetamol tablets',now()),
('35000000-0000-0000-0000-000000000003','33000000-0000-0000-0000-000000000003','إيبوبروفين','ايبوبروفين',now()),
('35000000-0000-0000-0000-000000000004','33000000-0000-0000-0000-000000000004','Augmentin 1g','augmentin 1g',now()),
('35000000-0000-0000-0000-000000000005','33000000-0000-0000-0000-000000000005','بخاخ فنتولين','بخاخ فنتولين',now()),
('35000000-0000-0000-0000-000000000006','33000000-0000-0000-0000-000000000007','Insulin pen','insulin pen',now()),
('35000000-0000-0000-0000-000000000007','33000000-0000-0000-0000-000000000009','نقط أوتريفين','نقط اوتريفين',now())
on conflict (medicine_id, normalized_alias) do update set alias=excluded.alias;

insert into medicine_package (id, medicine_id, strength_value, strength_unit, dosage_form_id,
 package_size_value, package_size_unit, route_of_administration, barcode, official_price, currency,
 status, active, search_text, created_at, updated_at, version) values
('34000000-0000-0000-0000-000000000001','33000000-0000-0000-0000-000000000001',500,'MG',(select id from dosage_form where code='TABLET'),24,'TABLET','ORAL','6221000001001',45.00,'EGP','AVAILABLE',true,'بانادول بنادول panadol paracetamol باراسيتامول 500 mg اقراص tablets 24 tablet 6221000001001',now(),now(),0),
('34000000-0000-0000-0000-000000000002','33000000-0000-0000-0000-000000000002',500,'MG',(select id from dosage_form where code='TABLET'),24,'TABLET','ORAL','6221000001002',60.00,'EGP','AVAILABLE',true,'بانادول اكسترا panadol extra paracetamol caffeine باراسيتامول كافيين 500 mg اقراص tablets 24 tablet 6221000001002',now(),now(),0),
('34000000-0000-0000-0000-000000000003','33000000-0000-0000-0000-000000000003',200,'MG',(select id from dosage_form where code='TABLET'),20,'TABLET','ORAL','6221000001003',35.00,'EGP','AVAILABLE',true,'بروفين brufen ibuprofen ايبوبروفين 200 mg اقراص tablets 20 tablet 6221000001003',now(),now(),0),
('34000000-0000-0000-0000-000000000004','33000000-0000-0000-0000-000000000003',400,'MG',(select id from dosage_form where code='TABLET'),30,'TABLET','ORAL','6221000001004',70.00,'EGP','AVAILABLE',true,'بروفين brufen ibuprofen ايبوبروفين 400 mg اقراص tablets 30 tablet 6221000001004',now(),now(),0),
('34000000-0000-0000-0000-000000000005','33000000-0000-0000-0000-000000000004',1000,'MG',(select id from dosage_form where code='TABLET'),14,'TABLET','ORAL','6221000001005',180.00,'EGP','AVAILABLE',true,'اوجمنتين augmentin amoxicillin clavulanic acid اموكسيسيلين حمض كلافولانيك 1g 1000 mg اقراص 14 tablet 6221000001005',now(),now(),0),
('34000000-0000-0000-0000-000000000006','33000000-0000-0000-0000-000000000005',100,'MCG',(select id from dosage_form where code='INHALER'),200,'DOSE','INHALATION','6221000001006',120.00,'EGP','AVAILABLE',true,'فنتولين ventolin salbutamol سالبوتامول 100 mcg بخاخ inhaler 200 dose 6221000001006',now(),now(),0),
('34000000-0000-0000-0000-000000000007','33000000-0000-0000-0000-000000000006',50,'MG',(select id from dosage_form where code='TABLET'),20,'TABLET','ORAL','6221000001007',85.00,'EGP','AVAILABLE',true,'كاتافلام cataflam diclofenac ديكلوفيناك 50 mg اقراص 20 tablet 6221000001007',now(),now(),0),
('34000000-0000-0000-0000-000000000008','33000000-0000-0000-0000-000000000007',100,'IU_ML',(select id from dosage_form where code='PREFILLED_PEN'),5,'PEN','SUBCUTANEOUS','6221000001008',650.00,'EGP','AVAILABLE',true,'لانتوس lantus insulin glargine انسولين جلارجين 100 iu ml قلم prefilled pen 5 pen 6221000001008',now(),now(),0),
('34000000-0000-0000-0000-000000000009','33000000-0000-0000-0000-000000000008',5,'MG',(select id from dosage_form where code='TABLET'),30,'TABLET','ORAL','6221000001009',95.00,'EGP','AVAILABLE',true,'كونكور concor bisoprolol بيسوبرولول 5 mg اقراص 30 tablet 6221000001009',now(),now(),0),
('34000000-0000-0000-0000-000000000010','33000000-0000-0000-0000-000000000009',0.1,'PERCENT',(select id from dosage_form where code='NASAL_DROPS'),10,'ML','NASAL','6221000001010',55.00,'EGP','AVAILABLE',true,'اوتريفين otrivin xylometazoline زيلوميتازولين 0.1 percent نقط للانف nasal drops 10 ml 6221000001010',now(),now(),0),
('34000000-0000-0000-0000-000000000011','33000000-0000-0000-0000-000000000010',50,'MG',(select id from dosage_form where code='CAPSULE'),20,'CAPSULE','ORAL','6221000001011',90.00,'EGP','UNSUPPORTED',true,'ترامادول tramadol 50 mg كبسولات capsules 20 capsule controlled restricted 6221000001011',now(),now(),0),
('34000000-0000-0000-0000-000000000012','33000000-0000-0000-0000-000000000011',150,'MG',(select id from dosage_form where code='TABLET'),20,'TABLET','ORAL','6221000001012',40.00,'EGP','RECALLED',true,'رانيتيدين ranitidine 150 mg اقراص tablets recalled 20 tablet 6221000001012',now(),now(),0),
('34000000-0000-0000-0000-000000000013','33000000-0000-0000-0000-000000000012',1,'MG',(select id from dosage_form where code='PREFILLED_PEN'),1,'PEN','SUBCUTANEOUS','6221000001013',1800.00,'EGP','UNAVAILABLE',true,'اوزمبيك ozempic semaglutide سيماجلوتايد 1 mg قلم prefilled pen unavailable 6221000001013',now(),now(),0)
on conflict (barcode) do update set medicine_id=excluded.medicine_id, strength_value=excluded.strength_value,
 strength_unit=excluded.strength_unit, dosage_form_id=excluded.dosage_form_id,
 package_size_value=excluded.package_size_value, package_size_unit=excluded.package_size_unit,
 route_of_administration=excluded.route_of_administration, official_price=excluded.official_price,
 currency=excluded.currency, status=excluded.status, active=excluded.active,
 search_text=excluded.search_text, updated_at=now();

insert into catalogue_status_history (id, medicine_package_id, old_status, new_status, old_active,
 new_active, actor_user_id, reason, created_at)
select gen.id, mp.id, null, mp.status, null, mp.active, null, 'Local sample catalogue seed', now()
from (values
 ('36000000-0000-0000-0000-000000000001'::uuid,'6221000001001'),
 ('36000000-0000-0000-0000-000000000002'::uuid,'6221000001002'),
 ('36000000-0000-0000-0000-000000000003'::uuid,'6221000001003'),
 ('36000000-0000-0000-0000-000000000004'::uuid,'6221000001004'),
 ('36000000-0000-0000-0000-000000000005'::uuid,'6221000001005'),
 ('36000000-0000-0000-0000-000000000006'::uuid,'6221000001006'),
 ('36000000-0000-0000-0000-000000000007'::uuid,'6221000001007'),
 ('36000000-0000-0000-0000-000000000008'::uuid,'6221000001008'),
 ('36000000-0000-0000-0000-000000000009'::uuid,'6221000001009'),
 ('36000000-0000-0000-0000-000000000010'::uuid,'6221000001010'),
 ('36000000-0000-0000-0000-000000000011'::uuid,'6221000001011'),
 ('36000000-0000-0000-0000-000000000012'::uuid,'6221000001012'),
 ('36000000-0000-0000-0000-000000000013'::uuid,'6221000001013')
) gen(id, barcode) join medicine_package mp on mp.barcode=gen.barcode
on conflict (id) do nothing;
