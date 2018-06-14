package com.yunji.titan.manager.common;

import java.io.File;
import java.util.List;

import com.yunji.titan.manager.bo.LinkBO;

public interface ILinkFileResolver {
	
	List<LinkBO> resolve(File file);

}
