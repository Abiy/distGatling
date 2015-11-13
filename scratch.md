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