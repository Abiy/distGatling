/*
 *
 *   Copyright 2016 Walmart Technology
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.walmart.gatling.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Created by walmart on 5/1/17.
 */
public class PageUtils {

    public static PageRequest getPageRequest( int size,  int page,  String sortBy) {
        PageRequest pageRequest;
        int pageNum = page > 0 ? page-1 : 0;
        int pageSize = size > 0 ? size : 1;
        if(StringUtils.isEmpty(sortBy))
            pageRequest= new PageRequest(pageNum,pageSize);
        else
            pageRequest= new PageRequest(pageNum,pageSize, Sort.Direction.DESC,sortBy);
        return pageRequest;
    }
}
