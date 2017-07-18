package com.ipfaffen.ovenbird.model.proxy;

/**
 * @author Isaias Pfaffenseller
 */
public class TransactionProxy {
	
	/**
	 * @param object
	 * @param connection
	 * @return
	 */
	/*@SuppressWarnings("unchecked")
	public static <T extends ModelDao<?>> T create(final T object) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(object.getClass());
		enhancer.setCallbackType(MethodInterceptor.class); 
		enhancer.setCallback(new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				Transactional annotation = method.getAnnotation(Transactional.class);
				if(annotation != null) {
					boolean commit = true;
					try {
						if(annotation.readOnly()) {
							object.openConnection();
						}
						else {
							object.openTransaction();
						}

						// Invoke method and return its value if provided.
						return method.invoke(object, args);
					}
					catch(Throwable t) {
						commit = false;
						throw t.getCause();
					}
					finally {
						if(annotation.readOnly()) {
							object.closeConnection();
						}
						else {
							object.closeTransaction(commit);
						}
					}
				}
				
				// Not transactional so just invoke method.
				return method.invoke(object, args);
			}
		});
		return (T) enhancer.create(new Class[]{Database.class}, new Object[]{object.getDatabase()});
	}*/
}
