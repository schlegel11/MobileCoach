<%@page import="java.util.Collections"%>
<%@page import="java.util.Comparator"%>
<%@page import="org.apache.logging.log4j.LogManager"%>
<%@page import="org.bson.types.ObjectId"%>
<%@page import="org.apache.commons.lang3.StringEscapeUtils"%>
<%@page import="ch.ethz.mc.conf.ImplementationConstants"%>
<%@page import="ch.ethz.mc.model.rest.Variable"%>
<%@page import="ch.ethz.mc.model.rest.ExtendedListVariable"%>
<%@page import="ch.ethz.mc.model.rest.CollectionOfExtendedListVariables"%>
<%@page import="ch.ethz.mc.model.rest.ExtendedVariable"%>
<%@page import="ch.ethz.mc.model.rest.CollectionOfExtendedVariables"%>
<%@page import="org.apache.logging.log4j.Logger"%>
<%@page import="ch.ethz.mc.services.RESTManagerService"%>
<%@page import="ch.ethz.mc.model.persistent.Participant"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Hashtable"%>
<%@page import="ch.ethz.mc.model.persistent.Intervention"%>
<%@page import="ch.ethz.mc.services.SurveyAdministrationManagerService"%>
<%@page
	import="ch.ethz.mc.services.InterventionAdministrationManagerService"%>
<%@page import="ch.ethz.mc.MC"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	String secret = "xladXk12hVresE";

	//Init
	Logger log = LogManager.getLogger("ch.ethz.mc.r4l-admin.jsp"); 
	
	if (!MC.getInstance().isReady()) {
		out.write("Context not ready.");
		out.flush();
		out.close();
		return;
	} else if (request.getParameter("secret") == null
	|| !request.getParameter("secret").equals(secret)) {
		out.write("Wrong secret.");
		out.flush();
		out.close();
		return;
	}
	
	InterventionAdministrationManagerService iam = MC.getInstance()
	.getInterventionAdministrationManagerService();
	SurveyAdministrationManagerService sam = MC.getInstance()
	.getSurveyAdministrationManagerService();
	RESTManagerService rms = MC.getInstance().getRestManagerService();
	
	// Data change
	if (request.getParameter("action") != null &&request.getParameter("challenge") != null && request.getParameter("participant") != null) {
		boolean action = Boolean.parseBoolean(request.getParameter("action"));
		String challenge = request.getParameter("challenge");
		ObjectId participantId = new ObjectId(request.getParameter("participant"));
		
		log.debug("Changing post of challenge {} of participant {} to {}", challenge, participantId, action);
		rms.writeVariable(participantId, challenge.replace("#", "NotAppropriate"), action?"0":"1", false, true);
	}


	// Configuration
	Hashtable<String, String> groups = new Hashtable<String, String>();
	groups.put("1", "Teilnehmer ab 06/2017");
	groups.put("2", "Teilnehmer ab 07/2017");
	groups.put("3", "Teilnehmer ab 08/2017");
	groups.put("4", "Teilnehmer ab 09/2017");
	groups.put("5", "Teilnehmer ab 10/2017");
	groups.put("6", "Teilnehmer ab 11/2017");
	groups.put("7", "Teilnehmer ab 12/2017");
	groups.put("8", "Teilnehmer ab 01/2018");
	groups.put("9", "Teilnehmer ab 02/2018");
	groups.put("10", "Teilnehmer ab 03/2018");
	groups.put("11", "Teilnehmer ab 04/2018");
	groups.put("12", "Teilnehmer ab 05/2018");
	groups.put("13", "Teilnehmer ab 06/2018");
	groups.put("14", "Teilnehmer ab 07/2018");
	groups.put("101", "Interne Testteilnehmer ab 06/2017");
	groups.put("102", "Interne Testteilnehmer ab 07/2017");
	groups.put("103", "Interne Testteilnehmer ab 08/2017");
	groups.put("104", "Interne Testteilnehmer ab 09/2017");
	groups.put("105", "Interne Testteilnehmer ab 10/2017");
	groups.put("106", "Interne Testteilnehmer ab 11/2017");
	groups.put("107", "Interne Testteilnehmer ab 12/2017");
	groups.put("108", "Interne Testteilnehmer ab 01/2018");
	groups.put("109", "Interne Testteilnehmer ab 02/2018");
	groups.put("110", "Interne Testteilnehmer ab 03/2018");
	groups.put("111", "Interne Testteilnehmer ab 04/2018");
	groups.put("112", "Interne Testteilnehmer ab 05/2018");
	groups.put("113", "Interne Testteilnehmer ab 06/2018");
	groups.put("114", "Interne Testteilnehmer ab 07/2018");

	Hashtable<String, String> challenges = new Hashtable<String, String>();
	challenges.put("challenge#01", "1. Challenge: Message Contest");
	challenges.put("challenge#02", "2. Challenge: Message Contest with Picture");

	// Data preparation
	Hashtable<String, Participant> groupParticipants = new Hashtable<String, Participant>();

	List<String> sortedGroups = new ArrayList<String>();
	sortedGroups.addAll(groups.keySet());
	Collections.sort(sortedGroups);

	List<String> sortedChallenges = new ArrayList<String>();
	sortedChallenges.addAll(challenges.keySet());
	Collections.sort(sortedChallenges);

	Intervention intervention = null;
	Iterable<Intervention> interventions = iam.getAllInterventions();

	for (Intervention interventionInLoop : interventions) {
		if (interventionInLoop.getName().toLowerCase()
		.startsWith("MCA 2")
		&& interventionInLoop.isActive()) {
	intervention = interventionInLoop;
	break;
		}
	}

	if (intervention == null) {
		out.write("Intervention not ready.");
		out.flush();
		out.close();
		return;
	}

	Iterable<Participant> participants = iam
	.getAllParticipantsOfIntervention(intervention.getId());

	for (String group : sortedGroups) {
		for (Participant participant : participants) {
	if (participant.getGroup() != null
	&& participant.getGroup().equals(group)) {
		groupParticipants.put(group, participant);
		break;
	}
		}
	}
%>
<%!public String esc(String string) {
		return StringEscapeUtils.escapeHtml4(string);
	}%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>MCA 2 Challenge Admin</title>
<style type="text/css">
body {
	font-family: Tahoma, Arial, sans-serif;
	padding: 20px 20px 20px 20px;
	font-size: 0.9em;
}

.ui-widget {
	font-family: Tahoma, Arial, sans-serif;
}

h1 {
	font-size: 1.5em;
	text-align: center;
}

h2 {
	color: darkgray;
	font-size: 1.3em;
}

h3 {
	color: gray;
	font-size: 1.2em;
}

p {
	font-size: 1em;
}

a {
	color: darkgray;
	text-decoration: none;
}

footer {
	padding-top: 20px;
}

table {
	border-spacing: 0px;
	border-collapse: collapse;
	width: 100%;
	margin-bottom: 2px;
	font-size: 0.9em;
}

th, td {
	border: 2px solid lightgray;
	padding: 2px 2px 2px 2px;
	vertical-align: middle;
	text-align: center;
	white-space: pre-line;
	width: 25%;
}

th {
	font-weight: bold;
	text-align: left;
	background-color: rgb(247, 247, 247);
	width: 20%;
}

.warn {
	color: red;
	font-weight: bold;
}

.green {
	background-color: green;
	color: white;
}

.red {
	background-color: red;
	color: white;
}

.bold {
	font-weight: bold;
}
</style>
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.0/jquery.min.js"></script>
<script type="text/javascript">
	var setAppropriate = function(action, challenge, participant) {
		$("#form").attr("action",
				"#post_" + challenge.replace("#", "_") + "_" + participant);
		$("#form").find("input[name='action']").attr("value", action);
		$("#form").find("input[name='challenge']").attr("value", challenge);
		$("#form").find("input[name='participant']").attr("value", participant);
		$("#form").submit();
	};
</script>
</head>
<body>
	<form id="form" method="POST" action="">
		<input type="hidden" name="action" value="" /> <input type="hidden"
			name="challenge" value="" /> <input type="hidden" name="participant"
			value="" />
	</form>
	<h1><%=intervention.getName()%></h1>
	<p>This page only shows images/comments of participants during
		posting and checking phase.</p>
	<%
		for (String group : sortedGroups) {
	%>
	<h2><%=groups.get(group)%></h2>
	<%
		Participant participant = groupParticipants.get(group);
			if (participant == null) {
	%>
	<p class="warn">No participant in this group.</p>
	<%
		} else {
				for (String challenge : sortedChallenges) {
	%>
	<h3><%=challenges.get(challenge)%></h3>
	<%
		List<String> variableList = new ArrayList<String>();
					variableList.add(challenge.replace("#", "Comment"));
					variableList.add(challenge.replace("#", "Image"));
					variableList.add(challenge.replace("#",
							"NotAppropriate"));

					CollectionOfExtendedListVariables result = rms
							.readVariableListArrayOfGroupOrIntervention(
									participant.getId(), variableList,
									true, true);

					int i = 0;
					out.write("<table><tr>");

					List<ExtendedListVariable> variableListing = result
							.getVariableListing();
					Hashtable<String, ExtendedListVariable> variableNameHashtable = new Hashtable<String, ExtendedListVariable>();

					List<String> variableNameListing = new ArrayList<String>();
					for (ExtendedListVariable post : variableListing) {
						variableNameListing.add(post.getIdentifier());
						variableNameHashtable.put(post.getIdentifier(),
								post);
					}
					Collections.sort(variableNameListing);

					for (String postName : variableNameListing) {
						ExtendedListVariable post = variableNameHashtable
								.get(postName);

						String comment = post.getVariables().get(0)
								.getValue();
						String image = post.getVariables().get(1)
								.getValue();
						boolean notAppropriate = post.getVariables().get(2)
								.getValue().equals("0") ? false : true;

						Variable statusVariable = rms.readVariable(
								new ObjectId(post.getIdentifier()),
								challenge.replace("#", "Status"), true);

						if ((statusVariable.getValue().equals("0") || statusVariable
								.getValue().equals("1"))
								&& (!comment.equals("") || !image
										.equals(""))) {
							i++;
	%>
	<td
		id="post_<%=challenge.replace("#", "_")%>_<%=post.getIdentifier()%>">
		<%
			if (!image.equals("")) {
		%> <a
		href="<%=ImplementationConstants.FILE_STREAMING_SERVLET_PATH
											+ "/" + image%>"
		target="_blank"><img
			src="<%=ImplementationConstants.FILE_STREAMING_SERVLET_PATH
											+ "/" + image%>/200/200/false/true" /></a><br />
		<%
			}
		%> <%=esc(comment)%><br /> <br /> <%
 	if (statusVariable.getValue().equals("1")) {
 							if (notAppropriate) {
 								out.write("<button onclick=\"setAppropriate(true, '"
 										+ challenge
 										+ "', '"
 										+ post.getIdentifier()
 										+ "')\" class=\"red\">Set appropriate</button>");
 							} else {
 								out.write("<button onclick=\"setAppropriate(false, '"
 										+ challenge
 										+ "', '"
 										+ post.getIdentifier()
 										+ "')\" class=\"green\">Set NOT appropriate</button>");
 							}
 						} else {
 %> <span class="bold">Still open for posting</span> <%
 	}
 %>
	</td>
	<%
		if (i == 4) {
								out.write("</tr><tr>");
								i = 0;
							}

						}
					}
					out.write("</tr></table>");
				}
			}
		}
	%>
</body>
</html>