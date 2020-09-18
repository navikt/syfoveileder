# Syfoveileder

## About Syfoveileder
Syfoveileder is a Spring Boot application written in Kotlin. Its main job is to be a microservice offering data relating
to veiledere. So far this means a list of veileder names with access to an enhet.


## Dependencies
The data quality of veileder in Azure AD is not good enough for veileder to enhet relations. It contains employee
relations to an enhet, but not access rights to an enhet. This data lives in Axsys. Veileder names on the other hand,
exists only in Azure AD.

Thus, Syfoveileder collects the veileder to enhet relation from Axsys, and the names from Azure AD's Graph Api. We collect the
name of the enhet, needed to fetch veileder names from Azure AD, from Norg2.

Sometimes we can't find the given veilederident in Azure AD, this can be because the veileder has quit, or because data has been entered incorrectly.
In these instances, we log the ident, then we can send it to a grown up, or ask for help in #tech-windows.  
<img src="https://upload.wikimedia.org/wikipedia/commons/9/9c/Clint_Eastwood1.png" alt="noname" width="200" height="88">
