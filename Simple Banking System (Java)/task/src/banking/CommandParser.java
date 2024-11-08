package banking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class StructParameter{
    String defaultValue = "";
    List<String> possibleValues = new ArrayList<>();

    public StructParameter(String defaultValue, List<String> possibleValues){
        this.defaultValue = defaultValue;
        this.possibleValues = possibleValues;
    }
}

class CommandLineParser{
    private HashMap<String, String> parameters = new HashMap<>();
    private HashMap<String, StructParameter> defaults = new HashMap<>();

    public void addParameter(String key, String defaultValue){
        defaults.put(key, new StructParameter(defaultValue, null));
    }

    public void addParameter(String key, String defaultValue, List<String>possibleValues){
        defaults.put(key, new StructParameter(defaultValue, possibleValues));
    }

    public void add(String key, String value){
        parameters.put(key, checkValue(key, value));
    }

    public void add(String[] args){
        for (int i = 0; i < args.length; i += 2) {
            if(i+1 >= args.length) return;
            add(args[i], args[i + 1]);
        }
    }

    private String checkValue(String key, String value){
        StructParameter param = defaults.get(key);
        if(param == null) return value;
        if(param.possibleValues != null){
            if(param.possibleValues.contains(value)) return value;
            return param.defaultValue;
        }
        if(value.isEmpty()) return param.defaultValue;
        return value;
    }

    public String getValue(String key){
        String str = parameters.get(key);
        if(str == null) return defaults.get(key).defaultValue;
        return str;
    }
}
