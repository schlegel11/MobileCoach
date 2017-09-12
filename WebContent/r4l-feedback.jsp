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
	//Init
	Logger log = LogManager.getLogger("ch.ethz.mc.r4l-feedback.jsp");

	if (!MC.getInstance().isReady()) {
		out.write("System offline.");
		out.flush();
		out.close();
		return;
	}

	// Data change
	if (request.getParameter("action") != null
			&& request.getParameter("action").equals("go")) {

		InterventionAdministrationManagerService iam = MC.getInstance()
				.getInterventionAdministrationManagerService();

		String lang = request.getParameter("lang");
		String password = request.getParameter("password");

		if (password.matches("[1|2|3|4|5|6|9|0][5|6|7]\\d\\d[6|8]")) {
			Intervention intervention = null;
			Intervention intervention1 = null;
			Intervention intervention2 = null;
			Iterable<Intervention> interventions = iam
					.getAllInterventions();

			for (Intervention interventionInLoop : interventions) {
				if (interventionInLoop.getName().toLowerCase()
						.startsWith("ready4life 2017 einstieg")
						&& interventionInLoop.isActive()) {
					intervention = interventionInLoop;
				} else if (interventionInLoop.getName().toLowerCase()
						.startsWith("ready4life 2017 modul 1")
						&& interventionInLoop.isActive()) {
					intervention1 = interventionInLoop;
				} else if (interventionInLoop.getName().toLowerCase()
						.startsWith("ready4life 2017 modul 2")
						&& interventionInLoop.isActive()) {
					intervention2 = interventionInLoop;
				}
			}

			if (intervention == null || intervention1 == null
					|| intervention2 == null) {
				out.write("Intervention not ready.");
				out.flush();
				out.close();
				return;
			}

			int countIntervention1 = iam.countParticipantsWithPassword(
					intervention1.getId(), password);
			int countIntervention2 = iam.countParticipantsWithPassword(
					intervention2.getId(), password);

			ObjectId appropriateInterventionId;
			int appropriateModule;

			if (countIntervention1 > countIntervention2) {
				appropriateInterventionId = intervention1.getId();
				appropriateModule = 1;
			} else {
				appropriateInterventionId = intervention2.getId();
				appropriateModule = 2;
			}

			log.debug("Redirecting to module {}: {} vs. {}", appropriateModule, countIntervention1, countIntervention2);
			
			response.sendRedirect("https://r4l.swiss/MC/dashboard/"
					+ appropriateInterventionId.toHexString() + "/"
					+ lang + appropriateModule + password);
		}

	}
%>
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
	text-align: center;
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
</head>
<body>
	<form id="form" method="POST" action="">
		<input type="hidden" name="action" value="go" />
		<p>&nbsp;</p>
		<h1>Klassenfeedback / Résultats du groupe</h1>
		<br /> <input type="radio" id="de" name="lang" value="1" checked>
		<label for="de"> Deutsch</label> <br /> <input type="radio" id="fr"
			name="lang" value="2"> <label for="fr"> Français</label> <br />
		<br /> Passwort / Mot de passe: <input type="password"
			name="password" value="" /> <br /> <br /> <input type="submit"
			value="Anmelden / S'identifier" />
	</form>
</body>
</html>