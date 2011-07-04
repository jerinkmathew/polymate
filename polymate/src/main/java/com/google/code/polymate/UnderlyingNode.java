/*
 *	Copyright (c) 2011 Marc Mai
 *
 *	Licensed under the MIT license: 
 *	http://www.opensource.org/licenses/mit-license.php
 *
 */
package com.google.code.polymate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * TODO javadoc
 * 
 * <br>
 * <br>
 * <small> Copyright (c) 2011 Marc Mai <br />
 * https://code.google.com/p/polymate/ </small> <br>
 * 
 * @author mai.marc@gmail.com<br>
 *         </i></small>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface UnderlyingNode {

}
