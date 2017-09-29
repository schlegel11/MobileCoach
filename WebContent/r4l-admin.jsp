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
	String secret = "ax912kCXSadu";

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
	groups.put("11", "Bern ab 02.10.");
	groups.put("12", "Bern ab 06.11.");
	groups.put("13", "Bern ab 04.12.");
	groups.put("21", "Basel ab 02.10.");
	groups.put("22", "Basel ab 06.11.");
	groups.put("23", "Basel ab 04.12.");
	groups.put("31", "Aargau ab 02.10.");
	groups.put("32", "Aargau ab 06.11.");
	groups.put("33", "Aargau ab 04.12.");
	groups.put("41", "Genf/Waadt ab 02.10.");
	groups.put("42", "Genf/Waadt ab 06.11.");
	groups.put("43", "Genf/Waadt ab 04.12.");
	groups.put("51", "St. Gallen ab 02.10.");
	groups.put("52", "St. Gallen ab 06.11.");
	groups.put("53", "St. Gallen ab 04.12.");
	groups.put("61", "Schwyz ab 02.10.");
	groups.put("62", "Schwyz ab 06.11.");
	groups.put("63", "Schwyz ab 04.12.");
	groups.put("91", "LUNGENLIGA ab 02.10.");
	groups.put("92", "LUNGENLIGA ab 06.11.");
	groups.put("93", "LUNGENLIGA ab 04.12.");
	groups.put("00", "Interne Testgruppe ab 18.09.");
	groups.put("01", "Interne Testgruppe ab 02.10.");
	groups.put("02", "Interne Testgruppe ab 06.11.");
	groups.put("03", "Interne Testgruppe ab 04.12.");
	
	Hashtable<String, String> challenges1 = new Hashtable<String, String>();
	challenges1.put("challenge#01", "M1 - 01. Challenge - Own positive activity picture + message");
	challenges1.put("challenge#02", "M1 - 02. Challenge - Own anti-stress picture + message");
	challenges1.put("challenge#04", "M1 - 04. Challenge - Own quit message");
	challenges1.put("challenge#05", "M1 - 05. Challenge - Own motivational message");
	challenges1.put("challenge#06", "M1 - 06. Challenge - Own resistance message");
	challenges1.put("challenge#21", "M1 - 21. Challenge - Booster Be You: Helpful strategy against stress");

	Hashtable<String, String> challenges2 = new Hashtable<String, String>();
	challenges2.put("challenge#01", "M2 - 01. Challenge - Own positive social competence picture + message");
	challenges2.put("challenge#02", "M2 - 02. Challenge - Challenge offline time");
	challenges2.put("challenge#03", "M2 - 03. Challenge - Own say NO message tabak");
	challenges2.put("challenge#04", "M2 - 04. Challenge - Own say NO message Substanzen allgemein");
	challenges2.put("challenge#22", "M2 - 22. Challenge - Booster Be Smart: Personal social competence strategy");

	// Data preparation
	Hashtable<String, Participant> groupParticipants1 = new Hashtable<String, Participant>();
	Hashtable<String, Participant> groupParticipants2 = new Hashtable<String, Participant>();

	List<String> sortedGroups = new ArrayList<String>();
	sortedGroups.addAll(groups.keySet());
	Collections.sort(sortedGroups);

	List<String> sortedChallenges1 = new ArrayList<String>();
	sortedChallenges1.addAll(challenges1.keySet());
	Collections.sort(sortedChallenges1);

	List<String> sortedChallenges2 = new ArrayList<String>();
	sortedChallenges2.addAll(challenges2.keySet());
	Collections.sort(sortedChallenges2);

	Intervention intervention1 = null;
	Intervention intervention2 = null;
	Iterable<Intervention> interventions = iam.getAllInterventions();

	for (Intervention interventionInLoop : interventions) {
		if (interventionInLoop.getName().toLowerCase()
		.startsWith("ready4life 2017 modul 1")
		&& interventionInLoop.isActive()) {
	intervention1 = interventionInLoop;
		} else if (interventionInLoop.getName().toLowerCase()
		.startsWith("ready4life 2017 modul 2")
		&& interventionInLoop.isActive()) {
	intervention2 = interventionInLoop;
		}
	}

	if (intervention1 == null ||intervention2 == null) {
		out.write("Intervention not ready.");
		out.flush();
		out.close();
		return;
	}

	Iterable<Participant> participants1 = iam
	.getAllParticipantsOfIntervention(intervention1.getId());

	for (String group : sortedGroups) {
		for (Participant participant : participants1) {
	if (participant.getGroup() != null
	&& participant.getGroup().equals(group)) {
		groupParticipants1.put(group, participant);
		break;
	}
		}
	}

	Iterable<Participant> participants2 = iam
	.getAllParticipantsOfIntervention(intervention2.getId());

	for (String group : sortedGroups) {
		for (Participant participant : participants2) {
	if (participant.getGroup() != null
	&& participant.getGroup().equals(group)) {
		groupParticipants2.put(group, participant);
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
<title>ready4life Challenge Admin</title>
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
	var setAppropriate = function(action, intervention, challenge, participant) {
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
	<h1>ready4life Module 1 & 2</h1>
	<p>This page only shows images/comments of participants during
		posting and checking phase.</p>
	<%
		for (int n = 0; n < 2; n++) {
			Intervention intervention;
			Hashtable<String, Participant> groupParticipants;
			Hashtable<String, String> challenges;
			List<String> sortedChallenges;

			if (n == 0) {
				intervention = intervention1;
				groupParticipants = groupParticipants1;
				challenges = challenges1;
				sortedChallenges = sortedChallenges1;
			} else {
				intervention = intervention2;
				groupParticipants = groupParticipants2;
				challenges = challenges2;
				sortedChallenges = sortedChallenges2;
			}
	%>
	<h1><%=intervention.getName()%></h1>
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
							boolean notAppropriate = post.getVariables()
									.get(2).getValue().equals("0") ? false
									: true;

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
		id="post_<%=n %>_<%=challenge.replace("#", "_")%>_<%=post.getIdentifier()%>">
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
 									out.write("<button onclick=\"setAppropriate(true, '"+n+"', '"
 											+ challenge
 											+ "', '"
 											+ post.getIdentifier()
 											+ "')\" class=\"red\">Set appropriate</button>");
 								} else {
 									out.write("<button onclick=\"setAppropriate(false, '"+n+"', '"
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
		}
	%>
</body>
</html>