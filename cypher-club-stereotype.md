
MATCH (n1:StoreStereotype) DETACH DELETE n1
MATCH (n:StoreZoneStereotype) DETACH DELETE n
MATCH (n:StoreIsleStereotype) DETACH DELETE n
MATCH (n:StoreSectionStereotype) DETACH DELETE n

CREATE (n1:StoreStereotype { entity:'StoreNode', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"type",
                                           "required":true,
                                           "expression":"eval.contains(m,v)",
                                           "Stereotype":,
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

 (n2:StoreZoneStereotype { entity:'StoreZone', template:  '[  {
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
 (n2)-[:BELONGS_TO]->(n1)

//Create the index to enforce only one instance of the meta data exists at a time
CREATE CONSTRAINT ON (n:StoreStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:StoreZoneStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:StoreDepartmentStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:StoreIsleStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:StoreSectionStereotype) ASSERT exists(n.entity)

CREATE CONSTRAINT ON (n1:StoreStereotype) ASSERT n1.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:StoreZoneStereotype) ASSERT n2.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:StoreDepartmentStereotype) ASSERT n2.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:StoreIsleStereotype) ASSERT n2.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:StoreSectionStereotype) ASSERT n2.entity IS UNIQUE

//Zone childs
MATCH (z:StoreZoneStereotype)
WITH z
CREATE (n1:StoreIsleStereotype { entity:'StoreIsel', template:  '[  {
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
(s1:StoreSectionStereotype { entity:'StoreSection', template:  '[  {
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
(l1:StoreLocationStereotype { entity:'StoreLocation', template:  '[  {
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





