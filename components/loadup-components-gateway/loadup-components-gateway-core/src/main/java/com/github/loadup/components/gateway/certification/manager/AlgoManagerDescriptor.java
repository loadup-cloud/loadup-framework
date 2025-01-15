package com.github.loadup.components.gateway.certification.manager;

/*-
 * #%L
 * loadup-components-gateway-core
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

/**
 * 算法管理描述
 */
//@XObject("AlgoManagerDesc")
public class AlgoManagerDescriptor {

	/**
	 * 组件的名字
	 */
	//@XNode("@name")
	private String name;

	//@XNodeSpring("@listener")
	private AlgoManager algoManager;

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("AlgoManagerDescriptor{");
		sb.append("algoManager=").append(algoManager);
		sb.append(", name='").append(name).append('\'');
		sb.append('}');
		return sb.toString();
	}

	/**
	 * Getter method for property <tt>algoManager<tt>.
	 */
	public AlgoManager getAlgoManager() {
		return algoManager;
	}

	/**
	 * Setter method for property <tt>algoManager<tt>.
	 */
	public void setAlgoManager(AlgoManager algoManager) {
		this.algoManager = algoManager;
	}

	/**
	 * Getter method for property <tt>name<tt>.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter method for property <tt>name<tt>.
	 */
	public void setName(String name) {
		this.name = name;
	}
}
