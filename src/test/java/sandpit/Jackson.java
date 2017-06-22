package sandpit;

import java.util.ArrayList;
import java.util.List;

import net.esb.entity.element.connector.IMapConnectorDefinition;
import net.esb.entity.element.connector.TcpInputConnectorDefinition;
import net.esb.json.JsonUtils;


public class Jackson {

	public static void main(String[] args) {
		List<IMapConnectorDefinition<?,?>> connectorDefinitions = new ArrayList<>();
		TcpInputConnectorDefinition con = new TcpInputConnectorDefinition();
		connectorDefinitions.add(con);
		String result = JsonUtils.toJsonString(connectorDefinitions);
		System.out.println(result);
	}

}
