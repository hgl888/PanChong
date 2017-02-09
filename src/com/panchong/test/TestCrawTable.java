package com.panchong.test;

import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TestCrawTable {
	public static void main(String[] args) {
		try {
			Document document = Jsoup.connect(
					"http://www.landnj.cn/LandInfo.aspx?flag=gongshi&Id=172")
					.get();
			
			if (document != null) {
				List<TableElement> tableElemts = DataTableUtil
						.getFitElement(document);
				if (tableElemts != null && tableElemts.size() > 0) {
					List<PropertyInfo> propertyInfos = TableUtil
							.extractPropertyInfos(tableElemts);
//					List<PropertyInfo> propertyInfos = TableUtil
//					.getTableValue((Element) tableElemts);
					if (propertyInfos != null && propertyInfos.size() > 0) {
						for (PropertyInfo propertyInfo : propertyInfos) {
							System.out.println(propertyInfo.getName() + "  "
									+ propertyInfo.getValue());
						}
						System.out.println("-----------------------------------");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
