
MATCH (n1:StoreStereotype) DETACH DELETE n1
MATCH (n:StoreTypeStereotype) DETACH DELETE n
MATCH (n:StoreAisleStereotype) DETACH DELETE n
MATCH (n:StoreRackStereotype) DETACH DELETE n
MATCH (n:StoreSlotStereotype) DETACH DELETE n
MATCH (n:StoreDepartmentStereotype) DETACH DELETE n

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
                                           "desc":"validation message", "defaultValue":"STORE"
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"nodeId",
                                           "required":true,
                                           "expression":"(v.length()== 36)",
                                           "metaData":null,
                                           "desc":"validation message", "defaultValue":null
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
                                           "desc":"validation message", "defaultValue":"ZONE1"
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"typeId",
                                           "required":true,
                                           "expression":"(v.length()== 36)",
                                           "metaData":null,
                                           "desc":"validation message", "defaultValue":null
                                         }
                                         ]
                                         }]'}),
 (n2)-[:BELONGS_TO_STEREOTYPE{minCardinality:"1",maxCardinality:"*"}]->(n1)

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
                                           "desc":"validation message", "defaultValue":null
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"aisleId",
                                           "required":true,
                                           "expression":"(v.length()== 36)",
                                           "metaData":null,
                                           "desc":"validation message", "defaultValue":null
                                         }
                                         ]
                                         }]'}),
(aisle)-[:BELONGS_TO_STEREOTYPE{minCardinality:"1",maxCardinality:"1"}]->(type),
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
                                           "desc":"validation message", "defaultValue":null
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"rackId",
                                           "required":true,
                                           "expression":"(v.length()== 36)",
                                           "metaData":null,
                                           "desc":"validation message", "defaultValue":null
                                         }
                                         ]
                                         }]'}),
(rack)-[:BELONGS_TO_STEREOTYPE{minCardinality:"1",maxCardinality:"1"}]->(aisle),
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
                                           "desc":"validation message", "defaultValue":null
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"slotId",
                                           "required":true,
                                           "expression":"(v.length()== 15)",
                                           "metaData":null,
                                           "desc":"validation message", "defaultValue":null
                                         }
                                         ]
                                         }]'}),
(slot)-[:BELONGS_TO_STEREOTYPE{minCardinality:"1",maxCardinality:"1"}]->(rack),
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
(l1)-[:BELONGS_TO_STEREOTYPE{minCardinality:"1",maxCardinality:"1"}]->(slot)

//Create department as a node
MATCH (a:StoreAisleStereotype),(b:StoreRackStereotype),(c:StoreSlotStereotype)
WITH a, b,c
CREATE (dept:StoreDepartmentStereotype { entity:'StoreDepartment', template:  '[  {
                                          "version":"1.0",
                                          "attributes":[
                                        {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"name",
                                           "required":true,
                                           "expression":"eval.contains(m,v)",
                                           "metaData":["PHARMACY","PRODUCE"],
                                           "desc":"validation message", "defaultValue":"PRODUCE"
                                         },
                                         {
                                           "validationType":"Expression",
                                           "type":"String",
                                           "name":"nodeId",
                                           "required":true,
                                           "expression":"(v.length()== 36)",
                                           "metaData":null,
                                           "desc":"validation message", "defaultValue":null
                                         }
                                         ]
                                         }]'}),
(a)-[:ASSIGNED_TO_STEREOTYPE{minCardinality:"0",maxCardinality:"1"}]->(dept),
(b)-[:ASSIGNED_TO_STEREOTYPE{minCardinality:"0",maxCardinality:"1"}]->(dept),
(c)-[:ASSIGNED_TO_STEREOTYPE{minCardinality:"0",maxCardinality:"1"}]->(dept)




