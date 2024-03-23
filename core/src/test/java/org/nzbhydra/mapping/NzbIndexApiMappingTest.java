/*
 *  (C) Copyright 2024 TheOtherP (theotherp@posteo.net)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.nzbhydra.mapping;

import org.junit.jupiter.api.Test;
import org.nzbhydra.Jackson;
import org.nzbhydra.mapping.nzbindex.NzbIndexRoot;

public class NzbIndexApiMappingTest {

    @Test
    public void shouldDeserialize() throws Exception {
        Jackson.JSON_MAPPER.readValue(getClass().getResourceAsStream("/org/nzbhydra/mapping/nzbindexApi.json"), NzbIndexRoot.class);
    }

    @Test
    public void shouldDeserialize2() throws Exception {
        Jackson.JSON_MAPPER.readValue(getClass().getResourceAsStream("/org/nzbhydra/mapping/nzbindexApi2.json"), NzbIndexRoot.class);
    }
}
