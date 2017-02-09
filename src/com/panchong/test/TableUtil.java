package com.panchong.test;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;




public class TableUtil {

	//����ı�����getAcrossTableValue�������� ����ı�����getTableValue��������
	public static List<PropertyInfo> extractPropertyInfos(List<TableElement> tableElemts)  {
			List<PropertyInfo> propertyInfos = new ArrayList<PropertyInfo>();
			if(tableElemts!=null&&tableElemts.size()>0){
				for (TableElement element : tableElemts) {
					if(element.isCross()){
						propertyInfos.addAll(TableUtil.getAcrossTableValue(element.getElement()));	
					}else{
						propertyInfos.addAll(TableUtil.getTableValue(element.getElement()));	
					}
				}
				return propertyInfos;
			}
			return null;
		}
	
	
	//�����������ݵı����ȡ��������ռ�����������Ϊrow
		public static List<PropertyInfo> getTableValue(Element tableElement) {
			Elements trElements = tableElement.getElementsByTag("tr");
			int row = getRowElement(trElements.get(0));
			List<String> propertys = parseTablePropertys(row,trElements);
			return  parsePropertyValue(row, propertys, trElements);
		}

		
		//��ÿ�е�Ԫ�ش���ptdELementsList�б���,��ÿ�е���������length Ȼ�����parsePropertyString�����Ե�һ��������ȡ�õ�������
		private static List<String> parseTablePropertys(int row,Elements trElements) {
			List<String> propertys = new ArrayList<String>();
			List<Elements> ptdELementsList = new ArrayList<Elements>();
			int[] lengths = new int[row]; // ����ÿ�еĳ���(����-��Ԫ�����)
			int[] index = new int[row]; // ÿ��λ��
			for (int i = 0; i < row; i++) {
				Element trElement = trElements.get(i);
				Elements elements = new Elements();
				Elements tdElements = trElement.getElementsByTag("td");
				Elements thElements = trElement.getElementsByTag("th");
				if(tdElements!=null&&tdElements.size()>0){
					elements.addAll(tdElements);
				}
				if(thElements!=null&&thElements.size()>0){
					elements.addAll(thElements);
				}
				ptdELementsList.add(elements);
				lengths[i] = elements.size();
			}
			parsePropertyString(propertys, index, lengths, 0, ptdELementsList);
			return propertys;
		}

		//��ȡ���ݵ�ֵ---������������֮����� ������ȡtdԪ�ؽ���parseValues��ȡ���ݵ�ֵ
		//flags��¼�е�״̬
		//valueFlags��¼ÿ������һ��ֵ�Զ����Ԫ���״̬
		//vtdElements��¼ÿ�е�Ԫ���Ԫ��td
		//sizeֵ��������
		//length��¼ÿ�еĵ�Ԫ��td��
		private static List<PropertyInfo> parsePropertyValue(int row, List<String> propertys,
				Elements trElements) {
			int propertysSize = propertys.size();
			List<Elements> vtdElements = new ArrayList<Elements>();
			int trsize = trElements.size();
			int size = trsize - row;
			int[] lengths = new int[size];
			boolean[] flags = new boolean[size];
			boolean[][] valueFlags = new boolean[size][propertysSize];
			
			for (int i = row; i < trsize; i++) {
				Element trElement = trElements.get(i);
				Elements velements = new Elements();
				Elements tdElements = trElement.getElementsByTag("td");
				Elements thElements = trElement.getElementsByTag("th");
				if(thElements!=null&&thElements.size()>0){
					velements.addAll(thElements);
				}
				if(tdElements!=null&&tdElements.size()>0){
					velements.addAll(tdElements);
				}
//				Elements tdElements = trElement.getElementsByTag("td");
				if (velements != null && velements.size() > 0) {
					lengths[i-row] = velements.size();
					vtdElements.add(velements);
					for(int j = 0 ; j < propertysSize;j++){
						valueFlags[i-row][j]= false;
					}
					flags[i-row] = true;
				} else {
					size--;
				}
			}
			return parseValues(propertys, vtdElements, size, lengths, flags,valueFlags);
		}

	
		//����һ�Զ��ֵ ��ȡ������rowspan��colspan��,�Ѷ�Ӧλ�õ�ֵ��ֵ,���޸ĸ�λ�õ�valueFlagΪtrue
		private static List<PropertyInfo> parseValues(List<String> propertys,
				List<Elements> vtdElements, int size, int[] lengths,
				boolean[] flags, boolean[][] valueFlags) {
			int propertysSize = propertys.size();
			String[][] pValueStrs = new String[size][propertysSize];
			for (int i = 0; i < size; i++) {
				Elements tdValueElements = vtdElements.get(i);
				int k = 0;
				for (int j = 0; j < lengths[i]; j++) {
					Element tdValueElement = null;
					try {
						tdValueElement = tdValueElements.get(j);
					} catch (Exception e) {
						flags[i] = false;
						break;
					}
					if(valueFlags[i][k]){
						while (valueFlags[i][k]) {
							k++;
						}
					}
					if (tdValueElement.hasAttr("rowspan")) {
						int rowspan = Integer.valueOf(tdValueElement.attr("rowspan"));
						int colspan = 1;
						if (tdValueElement.hasAttr("colspan")) {
							colspan = Integer.valueOf(tdValueElement.attr("colspan"));
						}
						if (rowspan > 1) {
							System.out.println(rowspan);
							for (int m = 0; m < rowspan; m++) {
								for (int n = 0; n < colspan; n++) {
									System.out.println("i = " + i +"m=" +m);
									try {
										valueFlags[i + m][k + n] = true;
										pValueStrs[i + m][k + n] = tdValueElement.text();
									} catch (Exception e) {
										System.out.println("i = " + i +"m=" +m +"k+n = "+ (k+n)+ "length");
									}
								}
							}
						}
					}else if (tdValueElement.hasAttr("colspan")) {
						int colspan = Integer.valueOf(tdValueElement
								.attr("colspan"));
						if (colspan > 1) {
							if (colspan >= size - 1) {
								flags[i] = false;
								break;
							}
							for (int m = 0; m < colspan; m++) {
								valueFlags[i][k] = true;
								pValueStrs[i+m][k] = tdValueElement.text();
							}
						}
					}else{
						if(tdValueElement!=null){
							pValueStrs[i][k] = tdValueElement.text();
						}
					}
					k++;
				}
			}
			List<PropertyInfo> propertyInfos = new ArrayList<PropertyInfo>();
			for (int i = 0; i < size; i++) {				
				if (flags[i]) {
					for (int j = 0; j < propertysSize; j++) {						
						if(propertys.get(j)!=null){
							PropertyInfo propertyValue = new PropertyInfo();
							propertyValue.setName(propertys.get(j));
							propertyValue.setValue(pValueStrs[i][j]);
							propertyInfos.add(propertyValue);
						}
					}
				}
			}
			return propertyInfos;
		}
		
		public static List<PropertyInfo> getAcrossTableValue(Element element)
				{	

			Elements trElements = element.getElementsByTag("tr");
			List<PropertyInfo> propertyValues = new ArrayList<PropertyInfo>();
			for(Element trElement:trElements){
				Elements tdElements = trElement.getElementsByTag("td");
				int size = tdElements.size();
				if(size % 2 ==0){
					PropertyInfo propertyValue = null;
					for (int i = 0; i < size; i++) {
						Element tdElement = tdElements.get(i);
						String value = tdElement.text();
						if (i % 2 == 0) {
							propertyValue = new PropertyInfo();						
							propertyValue.setName(StringUtil.parseString(value));						
						} else {	
							propertyValue.setValue(value);
							propertyValues.add(propertyValue);
						}
					}
				}
			}	
			return propertyValues;
		}

		//�����������е�td,ȡ����colspan,��������Ĵ���1��˵�����ǰ������С����Ĵ����,�����ڶ���ȡֵ
		private static void parsePropertyString(List<String> propertyStrs,
				int[] index, int[] lengths, int ind, List<Elements> tdElementsList) {
			Elements tdElements = tdElementsList.get(ind);
			for (int i = index[ind]; i < lengths[ind]; i++) {
				index[ind] = index[ind] + 1;
				Element tdElement = tdElements.get(i);
				String attribute = tdElement.attr("colspan");
				String value = StringUtil.parseString(tdElement.text());
//				if (!value.trim().equals("")) {
					if (attribute != null && !attribute.equals("")) {
						int col = Integer.valueOf(attribute);
						if (col > 1) {
							parsePropertyString(propertyStrs, index, lengths,
									ind + 1, tdElementsList);
						} else {
							propertyStrs.add(value);
						}
					} else {
						propertyStrs.add(value);
					}
//				}
			}
		}

	
		private static int getRowElement(Element trElement) {
			Elements tdElements = trElement.getElementsByTag("td");
			int row = 1;
			for (Element tdElement : tdElements) {
				String attribute = tdElement.attr("rowspan");
				if (attribute != null && !attribute.equals("")) {
					int rowSpan = Integer.valueOf(attribute);
					if (rowSpan > row) {
						row = rowSpan;
					}
				}
			}
			return row;
		}
}