-- find accounts to delete
select * from two_factor_user where loginid like 'jsmith%'


delete from two_factor_user_attr where user_uuid in (select uuid from two_factor_user where loginid like 'jsmith%');
delete from two_factor_browser where user_uuid in (select uuid from two_factor_user where loginid like 'jsmith%');
delete from two_factor_audit where user_uuid in (select uuid from two_factor_user where loginid like 'jsmith%');
delete from two_factor_user where loginid like 'jsmith%';

delete from two_factor_user_attr where user_uuid in (select uuid from two_factor_user where loginid like '%cosignloadtest');
delete from two_factor_browser where user_uuid in (select uuid from two_factor_user where loginid like '%cosignloadtest');
delete from two_factor_audit where user_uuid in (select uuid from two_factor_user where loginid like '%cosignloadtest');
delete from two_factor_user where loginid like '%cosignloadtest';

delete from two_factor_user_attr where user_uuid in (select uuid from two_factor_user where loginid in ('admorten', 'admorten@test.upenn.edu', 'bwh', 'campeau', 'choate', 'cosigntest', 'couch', 'danalane', 'dtenney', 'g0144750', 'ghamlin', 'harveycg', 'jbreen', 'jorj', 'mchyzer', 'mchyzer2', 'mchyzer3214', 'msirota', 'muthm', 'mvm', 'read', 'sadf', 'sdafsadf', 'sdf', 'sdfasdfa', 'sdfsf', 'wescraig', 'zigouras'));
delete from two_factor_browser where user_uuid in (select uuid from two_factor_user where loginid in ('admorten', 'admorten@test.upenn.edu', 'bwh', 'campeau', 'choate', 'cosigntest', 'couch', 'danalane', 'dtenney', 'g0144750', 'ghamlin', 'harveycg', 'jbreen', 'jorj', 'mchyzer', 'mchyzer2', 'mchyzer3214', 'msirota', 'muthm', 'mvm', 'read', 'sadf', 'sdafsadf', 'sdf', 'sdfasdfa', 'sdfsf', 'wescraig', 'zigouras'));
delete from two_factor_audit where user_uuid in (select uuid from two_factor_user where loginid in ('admorten', 'admorten@test.upenn.edu', 'bwh', 'campeau', 'choate', 'cosigntest', 'couch', 'danalane', 'dtenney', 'g0144750', 'ghamlin', 'harveycg', 'jbreen', 'jorj', 'mchyzer', 'mchyzer2', 'mchyzer3214', 'msirota', 'muthm', 'mvm', 'read', 'sadf', 'sdafsadf', 'sdf', 'sdfasdfa', 'sdfsf', 'wescraig', 'zigouras'));
delete from two_factor_user where loginid in ('admorten', 'admorten@test.upenn.edu', 'bwh', 'campeau', 'choate', 'cosigntest', 'couch', 'danalane', 'dtenney', 'g0144750', 'ghamlin', 'harveycg', 'jbreen', 'jorj', 'mchyzer', 'mchyzer2', 'mchyzer3214', 'msirota', 'muthm', 'mvm', 'read', 'sadf', 'sdafsadf', 'sdf', 'sdfasdfa', 'sdfsf', 'wescraig', 'zigouras');


delete from two_factor_user_attr where user_uuid in (select uuid from two_factor_user where loginid in ('12345', '123456'));
delete from two_factor_browser where user_uuid in (select uuid from two_factor_user where loginid in ('admorten', 'admorten@test.upenn.edu', 'bwh', 'campeau', 'choate', 'cosigntest', 'couch', 'danalane', 'dtenney', 'g0144750', 'ghamlin', 'harveycg', 'jbreen', 'jorj', 'mchyzer', 'mchyzer2', 'mchyzer3214', 'msirota', 'muthm', 'mvm', 'read', 'sadf', 'sdafsadf', 'sdf', 'sdfasdfa', 'sdfsf', 'wescraig', 'zigouras'));
delete from two_factor_audit where user_uuid in (select uuid from two_factor_user where loginid in ('admorten', 'admorten@test.upenn.edu', 'bwh', 'campeau', 'choate', 'cosigntest', 'couch', 'danalane', 'dtenney', 'g0144750', 'ghamlin', 'harveycg', 'jbreen', 'jorj', 'mchyzer', 'mchyzer2', 'mchyzer3214', 'msirota', 'muthm', 'mvm', 'read', 'sadf', 'sdafsadf', 'sdf', 'sdfasdfa', 'sdfsf', 'wescraig', 'zigouras'));
delete from two_factor_user where loginid in ('admorten', 'admorten@test.upenn.edu', 'bwh', 'campeau', 'choate', 'cosigntest', 'couch', 'danalane', 'dtenney', 'g0144750', 'ghamlin', 'harveycg', 'jbreen', 'jorj', 'mchyzer', 'mchyzer2', 'mchyzer3214', 'msirota', 'muthm', 'mvm', 'read', 'sadf', 'sdafsadf', 'sdf', 'sdfasdfa', 'sdfsf', 'wescraig', 'zigouras');


delete from two_factor_user_attr where user_uuid in (select uuid from two_factor_user tfu where not exists (select 1 from two_factor_user_attr tfua where tfua.user_uuid = tfu.uuid and tfua.attribute_name = 'opted_in' and tfua.attribute_value_string='T' ));
delete from two_factor_browser where user_uuid in (select uuid from two_factor_user tfu where not exists (select 1 from two_factor_user_attr tfua where tfua.user_uuid = tfu.uuid and tfua.attribute_name = 'opted_in' and tfua.attribute_value_string='T' ));
delete from two_factor_audit where user_uuid in (select uuid from two_factor_user tfu where not exists (select 1 from two_factor_user_attr tfua where tfua.user_uuid = tfu.uuid and tfua.attribute_name = 'opted_in' and tfua.attribute_value_string='T' ));
delete from two_factor_report_privilege tfrp where TFRP.USER_UUID in (select uuid from two_factor_user tfu where not exists (select 1 from two_factor_user_attr tfua where tfua.user_uuid = tfu.uuid and tfua.attribute_name = 'opted_in' and tfua.attribute_value_string='T' ));
update two_factor_device_serial tfds set TFDS.USER_UUID = null where TFDS.USER_UUID in  (select uuid from two_factor_user tfu where not exists (select 1 from two_factor_user_attr tfua where tfua.user_uuid = tfu.uuid and tfua.attribute_name = 'opted_in' and tfua.attribute_value_string='T' ));
delete from two_factor_user where uuid in (select uuid from two_factor_user tfu where not exists (select 1 from two_factor_user_attr tfua where tfua.user_uuid = tfu.uuid and tfua.attribute_name = 'opted_in' and tfua.attribute_value_string='T' ));



