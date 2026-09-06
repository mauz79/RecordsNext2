var dataGiornata = new Array()
// Immettere le date nel formato inglese: Mese Giorno Anno
// Gennaio = January
// Febbraio = February
// Marzo = March
// Maprile = April
// Maggio = May
// Giugno = June
// Luglio = July
// Agosto = August
// Settembre = September
// Ottobre = October
// Novembre = November
// Dicembre = December
dataGiornata[1] = "November 11 2002" 
dataGiornata[2] = "September 15 2002" 
dataGiornata[3] = "September 22 2002" 
dataGiornata[4] = "September 29 2002" 
dataGiornata[5] = "October 06 2002" 
dataGiornata[6] = "October 20 2002" 
dataGiornata[7] = "October 27 2002" 
dataGiornata[8] = "November 03 2002" 
dataGiornata[9] = "November 10 2002" 
dataGiornata[10] = "November 17 2002" 
dataGiornata[11] = "November 24 2002" 
dataGiornata[12] = "December 01 2002" 
dataGiornata[13] = "December 08 2002" 
dataGiornata[14] = "December 15 2002" 
dataGiornata[15] = "December 22 2002" 
dataGiornata[16] = "January 12 2003" 
dataGiornata[17] = "January 19 2003" 
dataGiornata[18] = "January 26 2003" 
dataGiornata[19] = "February 02 2003" 
dataGiornata[20] = "February 09 2003" 
dataGiornata[21] = "February 16 2003" 
dataGiornata[22] = "February 23 2003" 
dataGiornata[23] = "March 02 2003" 
dataGiornata[24] = "March 09 2003" 
dataGiornata[25] = "March 16 2003" 
dataGiornata[26] = "March 23 2003" 
dataGiornata[27] = "April 06 2003" 
dataGiornata[28] = "April 13 2003" 
dataGiornata[29] = "April 19 2003" 
dataGiornata[30] = "April 27 2003" 
dataGiornata[31] = "May 04 2003" 
dataGiornata[32] = "May 11 2003" 
dataGiornata[33] = "May 18 2003" 
dataGiornata[34] = "May 25 2003" 
function initArray() {  
	this.length = initArray.arguments.length
    for (var i = 0; i < this.length; i++)
    this[i+1] = initArray.arguments[i]
}
var DOWArray = new initArray("Dom","Lun","Mar","Mer","Gio","Ven","Sab")
var MOYArray = new initArray("Gen","Feb","Mar","Apr","Mag","Giu","Lug","Ago","Set","Ott","Nov","Dic")
var Year
for (t = 1; t < dataGiornata.length ;t++ ) {
	data = new Date(dataGiornata[t])
	Year = data.getYear()
	if (Year < 2000)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}
