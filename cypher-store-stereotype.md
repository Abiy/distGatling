
MATCH (n1:StoreStereotype) DETACH DELETE n1
MATCH (n:StoreTypeStereotype) DETACH DELETE n
MATCH (n:StoreAisleStereotype) DETACH DELETE n
MATCH (n:StoreRackStereotype) DETACH DELETE n
MATCH (n:StoreSlotStereotype) DETACH DELETE n

CREATE (n1:StoreStereotype { entity:'StoreNode', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"type",
                                           "required":true,
                                           "expression":"eval.contains(m,v)",
                                           "metaData":["STORE"],
                                           "defaultValue":"STORE"
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"nodeId",
                                           "required":true,
                                           "expression":"(v.length()== 36)",
                                           "metaData":null,
                                           "defaultValue":null
                                         }
                                         ]
                                         }]'}),

 (n2:StoreTypeStereotype { entity:'StoreType', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"type",
                                           "required":true,
                                           "expression":"eval.contains(m,v)",
                                           "metaData":[
                                                     "TYPE1",
                                                     "TYPE2",
                                                     "TYPE3"
                                                   ],
                                           "defaultValue":"ZONE1"
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"typeId",
                                           "required":true,
                                           "expression":"(v.length()== 36)",
                                           "metaData":null,
                                           "defaultValue":null
                                         }
                                         ]
                                         }]'}),
 (n2)-[:BELONGS_TO]->(n1)

//Create the index to enforce only one instance of the meta data exists at a time
CREATE CONSTRAINT ON (n:StoreStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:StoreTypeStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:StoreAisleStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:StoreRackStereotype) ASSERT exists(n.entity)
CREATE CONSTRAINT ON (n:StoreSlotStereotype) ASSERT exists(n.entity)

CREATE CONSTRAINT ON (n1:StoreStereotype) ASSERT n1.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:StoreTypeStereotype) ASSERT n2.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:StoreAisleStereotype) ASSERT n2.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:StoreRackStereotype) ASSERT n2.entity IS UNIQUE
CREATE CONSTRAINT ON (n2:StoreSlotStereotype) ASSERT n2.entity IS UNIQUE

//Type childs
MATCH (type:StoreTypeStereotype)
WITH type
CREATE (aisle:StoreAisleStereotype { entity:'StoreAisle', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"type",
                                           "required":true,
                                           "expression":"(v.length()>= 15)",
                                           "metaData":null,
                                           "defaultValue":null
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"aisleId",
                                           "required":true,
                                           "expression":"(v.length()== 36)",
                                           "metaData":null,
                                           "defaultValue":null
                                         }
                                         ]
                                         }]'}),
(aisle)-[:BELONGS_TO]->(type),
(rack:StoreRackStereotype { entity:'StoreRack', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"type",
                                           "required":true,
                                           "expression":"(v.length()>= 15)",
                                           "metaData":null,
                                           "defaultValue":null
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"rackId",
                                           "required":true,
                                           "expression":"(v.length()== 36)",
                                           "metaData":null,
                                           "defaultValue":null
                                         }
                                         ]
                                         }]'}),
(rack)-[:BELONGS_TO]->(aisle),
(slot:StoreSlotStereotype { entity:'StoreSlot', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"type",
                                           "required":true,
                                           "expression":"(v.length()>= 15)",
                                           "metaData":null,
                                           "defaultValue":null
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"slotId",
                                           "required":true,
                                           "expression":"(v.length()== 15)",
                                           "metaData":null,
                                           "defaultValue":null
                                         }
                                         ]
                                         }]'}),
(slot)-[:BELONGS_TO]->(rack),
(l1:StoreLocationStereotype { entity:'StoreLocation', template:  '[  {
                                        "version":"1.0",
                                        "attributes":[
                                      {
                                         "validationType":"Expression",
                                         "type":"String",
                                         "name":"type",
                                         "required":true,
                                         "expression":"(v.length()>= 15)",
                                         "metaData":null,
                                         "defaultValue":null
                                       },
                                       {
                                         "validationType":"Expression",
                                         "type":"String",
                                         "name":"locationId",
                                         "required":true,
                                         "expression":"(v.length()== 15)",
                                         "metaData":null,
                                         "defaultValue":null
                                       }
                                       ]
                                       }]'}),
(l1)-[:BELONGS_TO]->(slot)





