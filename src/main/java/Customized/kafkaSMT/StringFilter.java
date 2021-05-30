/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package Customized.kafkaSMT;

import java.io.IOException;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SimpleConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

public class StringFilter<R extends ConnectRecord<R>> implements Transformation<R> {

    private static final String TOPIC = "${topic}";
    private static final String FIELD_DEFAULT = "";
    public static final String OVERVIEW_DOC =
            "Update the record's topic field as a function of the original topic value and the record timestamp."
                    + "<p/>"
                    + "This is mainly useful for sink connectors, since the topic field is often used to determine the equivalent entity name in the destination system"
                    + "(e.g. database table or search index name).";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(ConfigName.TOPIC_FORMAT, ConfigDef.Type.STRING, "${topic}", ConfigDef.Importance.HIGH,
                    "Format string which can contain <code>${topic}</code> and <code>${timestamp}</code> as placeholders for the topic and timestamp, respectively.")
            .define(ConfigName.TOPIC_FIELD, ConfigDef.Type.STRING, FIELD_DEFAULT, ConfigDef.Importance.HIGH, "Test");
    
    private interface ConfigName {
         String TOPIC_FORMAT = "topic.format";
         String TOPIC_FIELD = "topic.field";
    }
    
    private static class configInternal {
        configInternal(String field, String format) {
            this.field = field;
            this.format = format;
        }
        String field;
        String format;
    }
    private String topicFormat;
    private String topicField;
    private configInternal configInternal;
    @Override
    public void configure(Map<String, ?> props) {
        final SimpleConfig config = new SimpleConfig(CONFIG_DEF, props);
        topicFormat = config.getString(ConfigName.TOPIC_FORMAT);
        topicField = config.getString(ConfigName.TOPIC_FIELD);
        configInternal = new configInternal(topicField, topicFormat);
    }
    @Override
    public R apply(R record) {
        try{
            String valuefromAPI;
            valuefromAPI = ExternalAPI.MyGETRequest();
            //To check whether there schema used
            Object _updatedValue = null;
            if(record.value() instanceof HashMap){
                final HashMap<String, Object> value = (HashMap)record.value();
                HashMap<String, Object> updatedValue = applyValueWithOutSchema(value, valuefromAPI);
                _updatedValue = updatedValue;
            }
            else if(record.value() instanceof Struct){
                final Schema schema = record.valueSchema();
                final Struct value = (Struct)record.value();
                Struct updatedValue = applyValueWithSchema(value, schema, valuefromAPI);
                _updatedValue = updatedValue;
            }
            return record.newRecord(
                    record.topic(), record.kafkaPartition(),
                    record.keySchema(), record.key(),
                    record.valueSchema(), _updatedValue,
                    record.timestamp()
            );
        }
        catch (IOException ex) {
            Logger.getLogger(StringFilter.class.getName()).log(Level.SEVERE, null, ex);
                    return record.newRecord(
                record.topic(), record.kafkaPartition(),
                record.keySchema(), record.key(),
                record.valueSchema(), "Error",
                record.timestamp()
        );
        }
    }
    
    private HashMap applyValueWithOutSchema(HashMap<String, Object> value, String valuefromAPI) throws IOException {
        if (value == null) {
            return null;
        }
        WriteInfo("applyValueWithOutSchema: ");
        for (String key : value.keySet()) {
            if (key.equals(configInternal.field)) {
                 WriteInfo("fieldName: " + key + " value: " + value.get(key));
                 value.put(key, value.get(key) + valuefromAPI);
             }
        }    
        return value;
    }
    
    //Below function used for JDBC Sink
    private Struct applyValueWithSchema(Struct value, Schema schema, String valuefromAPI) throws IOException {
        if (value == null) {
            return null;
        }
        Struct updatedValue = new Struct(schema);
        WriteInfo("valueschema: " + value.schema().fields());
        for (Field field : value.schema().fields()) {
            final Object updatedFieldValue;
            if (field.name().equals(configInternal.field)) {
                WriteInfo("fieldName: " + field.name() + " value: " + value.get(field));
                updatedFieldValue = value.get(field) + valuefromAPI;
            } else {
                updatedFieldValue = value.get(field);
            }
            updatedValue.put(field.name(), updatedFieldValue);
        }
        return updatedValue;
    }
    
        private Struct applyValueWithSchemaDebezium(Struct value, Schema schema, String valuefromAPI) throws IOException {
        if (value == null) {
            return null;
        }
        Struct updatedValue = new Struct(schema);
        WriteInfo("valueschema: " + value.schema().fields());
        
        for (Field field : value.schema().fields()) {
            final Object updatedFieldValue;
            if (field.name().equals("after")){
                WriteInfo("after: " + field.name());
                WriteInfo("Values: " + value.schema().field("after").schema().fields());
                Struct innerStruct = new Struct(field.schema());
                for(Field innerField : value.schema().field("after").schema().fields() ){
                    final Object innerUpdatedFieldValue;
                    WriteInfo("innerField: " + innerField.name() + "   ConfigField:" + configInternal.field);
                    if (innerField.name().equals(configInternal.field)) {
                        WriteInfo("after innerField: " + innerField.name());
                        innerUpdatedFieldValue = value.getStruct("after").get(configInternal.field) + valuefromAPI;
                    }
                    else{
                        innerUpdatedFieldValue = value.getStruct("after").get(innerField);
                    }
                    innerStruct.put(innerField.name(), innerUpdatedFieldValue);
                }
                updatedFieldValue = innerStruct;
            }
            else{
                updatedFieldValue = value.get(field);
            }
            updatedValue.put(field.name(), updatedFieldValue);
        }
        return updatedValue;
    }
    

    @Override
    public void close() {
    }

  @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }
    public void WriteInfo(String logs) throws IOException{
    Logger logger = Logger.getLogger("MyLog");  
    FileHandler fh;
    try {  
        // This block configure the logger with handler and formatter  
        fh = new FileHandler("/tmp/mylog.log");  
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);  
        // the following statement is used to log any messages  
        logger.info("Message: " + logs);
        }
    catch (SecurityException | IOException e) {  
            e.printStackTrace();  
        }  
}

}