
MATCH (n1:DcStereotype) DETACH DELETE n1

MATCH (n:DcZoneStereotype) DETACH DELETE n

MATCH (n:DcDepartmentStereotype) DETACH DELETE n
MATCH (n:DcIsleStereotype) DETACH DELETE n
MATCH (n:DcSectionStereotype) DETACH DELETE n

CREATE (n1:DcStereotype { entity:'DcNode', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"type",
                                           "required":true,
                                           "expression":"eval.contains(m,v)",
                                           "Stereotype":[
                                                     "DC",
                                                     "STORE",
                                                     "CLUB"
                                                   ],
                                           "defaultValue":"STORE"
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"nodeId",
                                           "required":true,
                                           "expression":"(v.length()== 15)",
                                           "Stereotype":null,
                                           "defaultValue":null
                                         }
                                         ]
                                         }]'}),

 (n2:DcZoneStereotype { entity:'DcZone', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"type",
                                           "required":true,
                                           "expression":"eval.contains(m,v)",
                                           "Stereotype":[
                                                     "ZONE1",
                                                     "ZONE2",
                                                     "ZONE3"
                                                   ],
                                           "defaultValue":"ZONE1"
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"zoneId",
                                           "required":true,
                                           "expression":"(v.length()== 36)",
                                           "Stereotype":null,
                                           "defaultValue":null
                                         }
                                         ]
                                         }]'}),
 (n2)-[:BELONGS_TO]->(n1),

 (n3:DcDepartmentStereotype { entity:'DcDepartment', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"name",
                                           "required":true,
                                           "expression":"eval.contains(m,v)",
                                           "Stereotype":[
                                                     "PHARMACY",
                                                     "PRODUCE",
                                                     "TIRE"
                                                   ],
                                           "defaultValue":"PRODUCE"
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"departmentId",
                                           "required":true,
                                           "expression":"(v.length()== 36)",
                                           "Stereotype":null,
                                           "defaultValue":null
                                         }
                                         ]
                                         }]'}),
 (n3)-[:BELONGS_TO]->(n1)

//Create the index to enforce only one instance of the meta data exists at a time
CREATE CONSTRAINT ON (n:DcStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:DcZoneStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:DcDepartmentStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:DcIsleStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:DcSectionStereotype) ASSERT exists(n.entity)

CREATE CONSTRAINT ON (n1:DcStereotype) ASSERT n1.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:DcZoneStereotype) ASSERT n2.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:DcDepartmentStereotype) ASSERT n2.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:DcIsleStereotype) ASSERT n2.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:DcSectionStereotype) ASSERT n2.entity IS UNIQUE

//Zone childs
MATCH (z:DcZoneStereotype)
WITH z
CREATE (n1:DcIsleStereotype { entity:'DcIsel', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"type",
                                           "required":true,
                                           "expression":"(v.length()>= 15)",
                                           "Stereotype":null,
                                           "defaultValue":null
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"isleId",
                                           "required":true,
                                           "expression":"(v.length()== 15)",
                                           "Stereotype":null,
                                           "defaultValue":null
                                         }
                                         ]
                                         }]'}),
(n1)-[:BELONGS_TO]->(z),
(s1:DcSectionStereotype { entity:'DcSection', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"type",
                                           "required":true,
                                           "expression":"(v.length()>= 15)",
                                           "Stereotype":null,
                                           "defaultValue":null
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"sectionId",
                                           "required":true,
                                           "expression":"(v.length()== 15)",
                                           "Stereotype":null,
                                           "defaultValue":null
                                         }
                                         ]
                                         }]'}),
(s1)-[:BELONGS_TO]->(n1),
(l1:DcLocationStereotype { entity:'DcLocation', template:  '[  {
                                        "version":"1.0",
                                        "attributes":[
                                      {
                                         "validationType":"Expression",
                                         "type":"String",
                                         "name":"type",
                                         "required":true,
                                         "expression":"(v.length()>= 15)",
                                         "Stereotype":null,
                                         "defaultValue":null
                                       },
                                       {
                                         "validationType":"Expression",
                                         "type":"String",
                                         "name":"locationId",
                                         "required":true,
                                         "expression":"(v.length()== 15)",
                                         "Stereotype":null,
                                         "defaultValue":null
                                       }
                                       ]
                                       }]'}),
(l1)-[:BELONGS_TO]->(s1)





