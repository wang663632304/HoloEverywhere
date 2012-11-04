
package org.holoeverywhere.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.json.JSONArray;
import org.json.JSONObject;

public class Document implements Parseable<JSONObject, Document> {
    public Map<String, Block> blocks = new HashMap<String, Block>();
    public Map<String, Block> data = new HashMap<String, Block>();
    public List<IncludeRow> include = new ArrayList<IncludeRow>();

    @SuppressWarnings("unchecked")
    @Override
    public Document parse(JSONObject json) {
        include.clear();
        blocks.clear();
        data.clear();
        if (json.has("include")) {
            JSONArray include = json.optJSONArray("include");
            for (int i = 0; i < include.length(); i++) {
                this.include.add(new IncludeRow().parse(include.optJSONObject(i)));
            }
        }
        if (json.has("blocks")) {
            JSONObject blocks = json.optJSONObject("blocks");
            Iterator<String> keys = blocks.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                this.blocks.put(key, new Block().parse(blocks.optJSONObject(key)));
            }
        }
        if (json.has("data")) {
            JSONObject data = json.optJSONObject("data");
            Iterator<String> keys = data.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                this.data.put(key, new Block().parse(data.optJSONObject(key)));
            }
        }
        return this;
    }

    public synchronized void process(XMLStreamWriter writer) throws XMLStreamException {
        Map<String, Block> blocks = new HashMap<String, Block>(this.blocks);
        Map<String, Block> data = new HashMap<String, Block>(this.data);
        for (IncludeRow i : include) {
            i.process(blocks, data);
        }
        for (Entry<String, Block> entry : data.entrySet()) {
            writer.writeStartElement("style");
            writer.writeAttribute("name", entry.getKey());
            entry.getValue().process(writer, blocks);
            writer.writeEndElement();
        }
    }
}