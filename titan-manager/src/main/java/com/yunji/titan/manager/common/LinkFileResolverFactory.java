package com.yunji.titan.manager.common;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkFileResolverFactory {
	
	@Autowired
	private HarLinkFileResolver harLinkFileResolver;

	public ILinkFileResolver create(File f){
		
	    String suffix = f.getName().substring(f.getName().lastIndexOf(".") + 1).toUpperCase();
	    LinkFileType type = LinkFileType.valueOf(suffix);
	    
	    switch (type) {
		case HAR:
			return harLinkFileResolver;
		case JMX:
			return harLinkFileResolver;
		default:
			return harLinkFileResolver;
		}
	}
}
