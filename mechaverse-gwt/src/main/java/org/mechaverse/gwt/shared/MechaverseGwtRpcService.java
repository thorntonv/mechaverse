package org.mechaverse.gwt.shared;

import org.mechaverse.api.service.MechaverseService;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("service")
public interface MechaverseGwtRpcService extends MechaverseService, RemoteService {}
