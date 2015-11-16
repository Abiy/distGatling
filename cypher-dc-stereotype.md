
MATCH (n1:DcStereotype) DETACH DELETE n1
MATCH (n:DcZoneStereotype) DETACH DELETE n
MATCH (n:DcAisleStereotype) DETACH DELETE n
MATCH (n:DcSectionStereotype) DETACH DELETE n

CREATE (n1:DcStereotype { entity:'DcNode', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                             {"validationType":"Expression","type":"String","name":"type","required":true,"expression":"eval.contains(m,v)","metaData":null,"desc":"validation message", "defaultValue":"STORE"},
                                             {"validationType":"Expression","type":"String","name":"nodeId","required":true,"expression":"(v.length()== 15)","metaData":null,"desc":"validation message", "defaultValue":null}
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
                                           "metaData":[
                                                     "ZONE1",
                                                     "ZONE2",
                                                     "ZONE3"
                                                   ],
                                           "desc":"validation message", "defaultValue":"ZONE1"
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"zoneId",
                                           "required":true,
                                           "expression":"(v.length()== 36)",
                                           "metaData":null,
                                           "desc":"validation message", "defaultValue":null
                                         }
                                         ]
                                         }]'}),
 (n2)-[:BELONGS_TO_STEREOTYPE{minCardinality:"1",maxCardinality:"*"}]->(n1)

//Create the index to enforce only one instance of the meta data exists at a time
CREATE CONSTRAINT ON (n:DcStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:DcZoneStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:DcAisleStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:DcSectionStereotype) ASSERT exists(n.entity)

CREATE CONSTRAINT ON (n1:DcStereotype) ASSERT n1.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:DcZoneStereotype) ASSERT n2.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:DcAisleStereotype) ASSERT n2.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:DcSectionStereotype) ASSERT n2.entity IS UNIQUE

//Zone childs
MATCH (z:DcZoneStereotype)
WITH z
CREATE (n1:DcAisleStereotype { entity:'DcAisle', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"type",
                                           "required":true,
                                           "expression":"(v.length()>= 15)",
                                           "metaData":null,
                                           "desc":"validation message", "defaultValue":null
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"aisleId",
                                           "required":true,
                                           "expression":"(v.length()== 15)",
                                           "metaData":null,
                                           "desc":"validation message", "defaultValue":null
                                         }
                                         ]
                                         }]'}),
(n1)-[:BELONGS_TO_STEREOTYPE{minCardinality:"1",maxCardinality:"*"}]->(z),
(s1:DcSectionStereotype { entity:'DcSection', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"type",
                                           "required":true,
                                           "expression":"(v.length()>= 15)",
                                           "metaData":null,
                                           "desc":"validation message", "defaultValue":null
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"sectionId",
                                           "required":true,
                                           "expression":"(v.length()== 15)",
                                           "metaData":null,
                                           "desc":"validation message", "defaultValue":null
                                         }
                                         ]
                                         }]'}),
(s1)-[:BELONGS_TO_STEREOTYPE{minCardinality:"1",maxCardinality:"*"}]->(n1),
(l1:DcLocationStereotype { entity:'DcLocation', template:  '[  {
                                        "version":"1.0",
                                        "attributes":[
                                      {
                                         "validationType":"Expression",
                                         "type":"String",
                                         "name":"type",
                                         "required":true,
                                         "expression":"(v.length()>= 15)",
                                         "metaData":null,
                                         "desc":"validation message", "defaultValue":null
                                       },
                                       {
                                         "validationType":"Expression",
                                         "type":"String",
                                         "name":"locationId",
                                         "required":true,
                                         "expression":"(v.length()== 15)",
                                         "metaData":null,
                                         "desc":"validation message", "defaultValue":null
                                       }
                                       ]
                                       }]'}),
(l1)-[:BELONGS_TO_STEREOTYPE{minCardinality:"1",maxCardinality:"*"}]->(s1)





