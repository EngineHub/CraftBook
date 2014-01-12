package com.me4502.util;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import com.sk89q.craftbook.common.LanguageManager;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;

public class GenerateDefaultLanguage {

    public static void main(String[] args) {

        if(!new File("en_US.yml").exists()) {
            try {
                new File("en_US.yml").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            new File("en_US.yml").delete();
            try {
                new File("en_US.yml").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YAMLProcessor proc = new YAMLProcessor(new File("en_US.yml"), true, YAMLFormat.EXTENDED);
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
