-- restore buildContentIds from buildLogs for builds that were not successful f.e. failed after
-- promotion in Indy
-- Purpose for this script is to have consistency between Indy and PNC DB, for more see NCL-4186
-- subQuery extracts buildContentId from indy http path and update restores buildContendIds

update buildrecord
set buildcontentid = subquery.bcid
from
	(select substring(buildlog from '/track/(.*?)/') as bcid,id
	from buildrecord
	where buildcontentid is null
	and buildlog like '%http://indy%')
		as subquery
where buildrecord.id = subquery.id
