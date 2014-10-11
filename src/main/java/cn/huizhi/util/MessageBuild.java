package cn.huizhi.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;

public class MessageBuild {

	public static class FieldObject {
		public String name;
		public JavaType javaType;
		public FieldObject(String name, JavaType javaType) {
			this.name = name;
			this.javaType = javaType;
		}
	}
	public static void build(Message.Builder builder, Map<String, Object> keyvalues) {
		try {
			builder.clear();
			Descriptor descriptor = builder.getDescriptorForType();
			List<FieldDescriptor> list = descriptor.getFields();
			for (FieldDescriptor field : list) {
				if (field.isRepeated()) {//判断该字段是否repeated
					// 有多个参数的时候进行分割，并逐一设置repeated赋值
					Object value = keyvalues.get(field.getName());
					if(value == null) continue;
					String[] repeatedValue = value.toString().split(",");//分割参数
					Descriptor descriptor2 = field.getMessageType();
					DynamicMessage.Builder repeatBuilder = DynamicMessage.newBuilder(descriptor2);
					Map<String, Object> map = Maps.newHashMap();
			        //逐一赋值
					for (int j = 0; j < repeatedValue.length; j++) {
						try {
							String[] fieldValue = repeatedValue[j].split(":");
							if(StringUtils.isNumeric(fieldValue[1])) {
								map.put(fieldValue[0], Integer.parseInt(fieldValue[1]));
							} else {
								map.put(fieldValue[0], fieldValue[1]);
							}
							
			            } catch (IllegalArgumentException e) {//异常捕捉
			            	e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					build(repeatBuilder, map);
					builder.addRepeatedField(field, repeatBuilder.build());
				} else {
					String fieldName = field.getName();
					Object fieldValue = keyvalues.get(fieldName);
					if(fieldValue != null && !"".equals(fieldValue)) {
						builder.setField(field, fieldValue);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public static List<FieldObject> getFields(Message.Builder builder) {
		Descriptor descriptor = builder.getDescriptorForType();
		List<FieldDescriptor> list = descriptor.getFields();
		List<FieldObject> fields = Lists.newArrayList();
		for(FieldDescriptor field : list) {
			JavaType javaType = field.getJavaType();
			String name = field.getName();
			fields.add(new FieldObject(name, javaType));
		}
		return fields;
	}
}
