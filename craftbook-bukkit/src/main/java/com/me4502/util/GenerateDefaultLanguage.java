/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.me4502.util;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import com.sk89q.craftbook.core.LanguageManager;
import com.sk89q.craftbook.util.developer.ExternalUtilityBase;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class GenerateDefaultLanguage extends ExternalUtilityBase {

    public GenerateDefaultLanguage (String[] args) {
        super(args);
    }

    @Override
    public void generate(String[] args) {

        File language = new File(getGenerationFolder(), "en_US.yml");

        if(!language.exists()) {
            try {
                language.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            language.delete();
            try {
                language.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YAMLProcessor proc = new YAMLProcessor(language, true, YAMLFormat.EXTENDED);
        try {
            proc.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        proc.setWriteDefaults(true);

        new LanguageManager();

        for(Entry<String, String> map : LanguageManager.defaultMessages.entrySet()) {
            proc.getString(map.getKey(), map.getValue());
        }

        proc.save();
    }
}
